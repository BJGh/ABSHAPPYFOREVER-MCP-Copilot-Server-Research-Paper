using System.Collections.Concurrent;
using System.Globalization;
using System.Text.Json.Serialization;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.Hosting;

var builder = WebApplication.CreateBuilder(args);
var app = builder.Build();

record ProcessRequest(string[] Files);
record AggregatedRow(string user_id_, string session_, double x_coordinate_mean, double y_coordinate_mean);

app.MapPost("/process", async (HttpRequest req) =>
{
    var payload = await req.ReadFromJsonAsync<ProcessRequest>();
    if (payload == null || payload.Files == null || payload.Files.Length == 0)
        return Results.BadRequest(new { error = "No files provided" });

    // key = (user, session)
    var sums = new ConcurrentDictionary<string, (double sumX, double sumY, int count, long firstIndex)>();
    long index = 0;
    foreach (var file in payload.Files)
    {
        if (!File.Exists(file)) continue;
        using var sr = new StreamReader(file);
        string header = await sr.ReadLineAsync();
        if (header == null) continue;
        var cols = header.Split(',');
        int idxUser = Array.FindIndex(cols, c => c.Trim().Equals("user_id", StringComparison.OrdinalIgnoreCase));
        int idxSession = Array.FindIndex(cols, c => c.Trim().Equals("session", StringComparison.OrdinalIgnoreCase));
        int idxX = Array.FindIndex(cols, c => c.Trim().Equals("x_coordinate", StringComparison.OrdinalIgnoreCase));
        int idxY = Array.FindIndex(cols, c => c.Trim().Equals("y_coordinate", StringComparison.OrdinalIgnoreCase));
        if (idxUser < 0 || idxSession < 0 || idxX < 0 || idxY < 0) continue;

        while (!sr.EndOfStream)
        {
            var line = await sr.ReadLineAsync();
            if (string.IsNullOrWhiteSpace(line)) continue;
            var parts = line.Split(',');
            if (parts.Length <= Math.Max(Math.Max(idxUser, idxSession), Math.Max(idxX, idxY))) continue;
            string user = parts[idxUser].Trim();
            string session = parts[idxSession].Trim();
            if (string.IsNullOrEmpty(user)) continue;
            double x = 0, y = 0;
            double.TryParse(parts[idxX], NumberStyles.Float, CultureInfo.InvariantCulture, out x);
            double.TryParse(parts[idxY], NumberStyles.Float, CultureInfo.InvariantCulture, out y);

            string key = user + "||" + session;
            sums.AddOrUpdate(key,
                (_) => (x, y, 1, index),
                (_, old) => (old.sumX + x, old.sumY + y, old.count + 1, Math.Min(old.firstIndex, index))
            );
            index++;
        }
    }

    // Compute means and then keep only the first (by firstIndex) record per user (mimic original drop_duplicates behavior)
    var groupedByUser = new Dictionary<string, (string key, double meanX, double meanY, long firstIndex)>();
    foreach (var kv in sums)
    {
        var parts = kv.Key.Split("||");
        var user = parts[0];
        var session = parts.Length > 1 ? parts[1] : "";
        var (sumX, sumY, count, firstIdx) = kv.Value;
        var meanX = sumX / Math.Max(1, count);
        var meanY = sumY / Math.Max(1, count);
        if (!groupedByUser.TryGetValue(user, out var existing) || firstIdx < existing.firstIndex)
        {
            groupedByUser[user] = (kv.Key, meanX, meanY, firstIdx);
        }
    }

    var result = groupedByUser.Values
        .OrderBy(v => v.firstIndex)
        .Select(v =>
        {
            var parts = v.key.Split("||");
            var user = parts[0];
            var session = parts.Length > 1 ? parts[1] : "";
            return new AggregatedRow(user, session, v.meanX, v.meanY);
        })
        .ToArray();

    return Results.Json(result);
});

app.Run();
