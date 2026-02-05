import json
import sqlite3
import pandas as pd
import numpy as np
import requests
import pytest
from pathlib import Path
from sklearn.utils import shuffle
from classifier_service.nn_oop import BrowserFPClassifier

# Absolute import as required


class DummyResponse:
    def __init__(self, data, status_code=200):
        self._data = data
        self.status_code = status_code

    def json(self):
        return self._data


def _create_fingerprint_db(db_path: Path, rows):
    conn = sqlite3.connect(str(db_path))
    cur = conn.cursor()
    cur.execute("CREATE TABLE fingerprints (data TEXT)")
    for r in rows:
        cur.execute("INSERT INTO fingerprints (data) VALUES (?)", (json.dumps(r),))
    conn.commit()
    conn.close()


def test_full_pipeline_with_mocked_service(tmp_path, monkeypatch):
    # Prepare sample fingerprint rows (3 samples)
    rows = [
        {
            "plugins": {"value": ["p1", "p2"]},
            "platform": {"value": "win"},
            "timezone": {"value": "UTC+0"},
            "hardwareConcurrency": {"value": 4},
        },
        {
            "plugins": {"value": ["p1"]},
            "platform": {"value": "linux"},
            "timezone": {"value": "UTC+2"},
            "hardwareConcurrency": {"value": 2},
        },
        {
            "plugins": {"value": []},
            "platform": {"value": "mac"},
            "timezone": {"value": "UTC+0"},
            "hardwareConcurrency": {"value": 8},
        },
    ]

    db_file = tmp_path / "fp.db"
    _create_fingerprint_db(db_file, rows)

    # Mock C# service to return interaction features matching ids 0,1,2
    interaction_json = [
        {"user_id_": "0", "session_": "s0", "x_coordinate_mean": 0.1, "y_coordinate_mean": 0.2},
        {"user_id_": "1", "session_": "s1", "x_coordinate_mean": 0.3, "y_coordinate_mean": 0.4},
        {"user_id_": "2", "session_": "s2", "x_coordinate_mean": 0.5, "y_coordinate_mean": 0.6},
    ]
    monkeypatch.setattr(requests, "post", lambda *a, **k: DummyResponse(interaction_json, 200))

    # Instantiate classifier with small LSTM and short sequences for fast tests
    clf = BrowserFPClassifier(
        db_path=str(db_file),
        max_timesteps=5,
        lstm_units=[4],
        lstm_dropout=0.1,
        random_state=42,
    )

    # Run pipeline steps
    clf.connect_db()
    clf.load_fingerprint_data()
    assert clf.fingerprint_df is not None and len(clf.fingerprint_df) == 3

    clf.load_interaction_data()
    assert hasattr(clf, "merged_df")
    assert clf.merged_df.shape[0] == 3

    clf.normalize_features()
    # normalized_df should have values in [0,1]
    assert ((clf.normalized_df.values >= -1e-8) & (clf.normalized_df.values <= 1 + 1e-8)).all()

    clf.encode_targets()
    # label_encoders should include the target columns
    assert set(clf.label_encoders.keys()) == set(clf.target_columns)
    for k, v in clf.num_classes_dict.items():
        assert v >= 1

    dyn = clf.prepare_dynamic_data()
    assert dyn.shape == (3, clf.max_timesteps, 1)

    clf.split_data()
    # Ensure splits exist and y_train length equals number of targets
    assert hasattr(clf, "X_train_static") and hasattr(clf, "X_train_dynamic")
    assert len(clf.y_train) == len(clf.target_columns)

    clf.build_model()
    assert clf.model is not None
    # Output layer names should match target_columns
    out_names = {o.name.split("/")[0] if "/" in o.name else o.name for o in clf.model.output}
    assert set(clf.target_columns) <= out_names

    # Train one epoch to check training loop works (small data)
    history = clf.train(epochs=1, batch_size=1)
    assert hasattr(history, "history") and "loss" in history.history

    # Evaluate and visualize should run without crashing
    clf.evaluate_and_visualize()


def test_fallback_to_csv_processing(tmp_path, monkeypatch):
    # Create DB with two fingerprint rows
    rows = [
        {
            "plugins": {"value": ["a"]},
            "platform": {"value": "p1"},
            "timezone": {"value": "t1"},
            "hardwareConcurrency": {"value": 1},
        },
        {
            "plugins": {"value": ["b"]},
            "platform": {"value": "p2"},
            "timezone": {"value": "t2"},
            "hardwareConcurrency": {"value": 2},
        },
    ]
    db_file = tmp_path / "fp2.db"
    _create_fingerprint_db(db_file, rows)

    # Create a small CSV that will be used if requests.post fails
    csv_path = tmp_path / "interaction.csv"
    df_csv = pd.DataFrame(
        [
            {"user_id": "0", "session": "s0", "x_coordinate": 1.0, "y_coordinate": 2.0},
            {"user_id": "1", "session": "s1", "x_coordinate": 3.0, "y_coordinate": 4.0},
        ]
    )
    df_csv.to_csv(csv_path, index=False)

    # Force requests.post to raise to trigger fallback
    def raise_conn(*a, **k):
        raise requests.exceptions.ConnectionError("unavailable")

    monkeypatch.setattr(requests, "post", raise_conn)

    clf = BrowserFPClassifier(
        db_path=str(db_file),
        interaction_files=[str(csv_path)],
        max_timesteps=4,
        lstm_units=[2],
        lstm_dropout=0.0,
        random_state=0,
    )

    clf.connect_db()
    clf.load_fingerprint_data()
    # Should not raise when falling back
    clf.load_interaction_data()
    assert clf.merged_df.shape[0] >= 1
    clf.normalize_features()
    clf.encode_targets()
    # prepare_dynamic_data should produce sequences with requested length
    dyn = clf.prepare_dynamic_data()
    assert dyn.shape[1] == clf.max_timesteps