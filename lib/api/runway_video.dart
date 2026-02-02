// ignore_for_file: depend_on_referenced_packages

import 'dart:convert';
import 'dart:async';
import 'package:flutter/material.dart';
import 'package:video_player/video_player.dart';

// Appwrite
import 'package:appwrite/appwrite.dart';
import 'package:appwrite/models.dart' as aw;

class VideoGenerator extends StatefulWidget {
  final String data_uri_image;
  final String user_prompt;

  // IMPORTANT: pass the same Appwrite Client you use for auth/navigation
  final Client appwriteClient;

  // Your Appwrite Function ID (the router-based Python function)
  final String functionId;

  const VideoGenerator({
    Key? key,
    required this.data_uri_image,
    required this.user_prompt,
    required this.appwriteClient,
    required this.functionId,
  }) : super(key: key);

  @override
  VideoGeneratorState createState() => VideoGeneratorState();
}

class VideoGeneratorState extends State<VideoGenerator> {
  VideoPlayerController? _videoPlayerController;

  String? videoUrl;
  String? taskId;
  String? jobId;
  String? progressText;

  bool isLoading = false;
  bool isCanceling = false;
  bool isGenerating = false;
  bool isPolling = false;

  late final Functions _functions;

  @override
  void initState() {
    super.initState();
    _functions = Functions(widget.appwriteClient);
  }

  Future<void> generateVideo() async {
    setState(() {
      isLoading = true;
      isCanceling = false;
      isGenerating = true;
      videoUrl = null;
      taskId = null;
      jobId = null;
      progressText = 'Starting...';
    });

    try {
      final body = {
        "action": "create",
        "model": "gen3a_turbo", // or 'gen4_turbo' if that’s what you use
        "prompt_image": widget.data_uri_image,
        "prompt_text": widget.user_prompt,
        "duration": 5,
        "ratio": "1280:720",
      };

      final aw.Execution exec = await _functions.createExecution(
        functionId: widget.functionId,
        body: jsonEncode(body),
      );

      // You can also inspect exec.responseStatusCode if you want to handle non-200s differently
      final Map<String, dynamic> res = _safeDecode(exec.responseBody);

      if (res["ok"] == true && res["task_id"] != null) {
        setState(() {
          taskId = res["task_id"] as String?;
          jobId = res["job_id"] as String?;
          progressText = 'Queued...';
        });
        await _pollTaskStatus(); // will update UI as it goes
      } else if (res["not_allowed"] == true) {
        final ent = res["entitlement"];
        final requiresSub = ent != null ? ent["requires_subscription"] == true : false;
        setState(() {
          isPolling = false;
          progressText = null;
          videoUrl = requiresSub
              ? 'Subscription required to generate video.'
              : 'You do not have enough credits to generate a video.';
        });
      } else {
        setState(() {
          isPolling = false;
          progressText = null;
          videoUrl = 'Failed to start task: ${res["error"] ?? "Unknown error"}';
        });
      }
    } catch (e) {
      setState(() {
        videoUrl = 'Error starting task: $e';
      });
    } finally {
      setState(() {
        isLoading = false;
        isGenerating = false;
      });
    }
  }

  Future<void> _pollTaskStatus() async {
    if (taskId == null) return;

    isPolling = true;
    try {
      while (isPolling && taskId != null) {
        final body = {
          "action": "status",
          "task_id": taskId,
          // job_id helps finalize credit tracking in backend
          if (jobId != null) "job_id": jobId,
        };

        final aw.Execution exec = await _functions.createExecution(
          functionId: widget.functionId,
          body: jsonEncode(body),
        );
        final Map<String, dynamic> res = _safeDecode(exec.responseBody);

        if (res["ok"] == true) {
          final status = (res["status"] ?? "UNKNOWN").toString();
          // Some SDKs include progress; if not, show textual status
          final task = res["task"];
          final progress = _extractProgress(task); // returns "NN%" or null

          setState(() {
            progressText = progress ?? status;
          });

          if (status == "SUCCEEDED") {
            final String? url = (res["output_url"] as String?);
            setState(() {
              videoUrl = url ?? 'No output URL returned';
              isPolling = false;
              progressText = null;
            });
            if (videoUrl != null && videoUrl!.startsWith('http')) {
              _videoPlayerController = VideoPlayerController.networkUrl(Uri.parse(videoUrl!))
                ..initialize().then((_) {
                  setState(() {
                    _videoPlayerController?.setLooping(true);
                  });
                  _videoPlayerController?.play();
                });
            }
            break;
          } else if (status == "FAILED") {
            setState(() {
              videoUrl = 'Video generation failed';
              isPolling = false;
              progressText = null;
            });
            break;
          }
        } else {
          setState(() {
            videoUrl = 'Status error: ${res["error"] ?? res}';
            isPolling = false;
            progressText = null;
          });
          break;
        }

        await Future.delayed(const Duration(seconds: 3));
      }
    } catch (e) {
      setState(() {
        videoUrl = 'Error checking task status: $e';
        isPolling = false;
        progressText = null;
      });
    }
  }

  Future<void> cancelTask() async {
    if (taskId == null || isCanceling) return;

    setState(() {
      isCanceling = true;
      progressText = 'Canceling...';
    });

    try {
      final body = {
        "action": "cancel",
        "task_id": taskId,
        if (jobId != null) "job_id": jobId,
      };

      final aw.Execution exec = await _functions.createExecution(
        functionId: widget.functionId,
        body: jsonEncode(body),
      );
      final Map<String, dynamic> res = _safeDecode(exec.responseBody);

      if (res["ok"] == true) {
        setState(() {
          videoUrl = 'Task canceled successfully.';
          taskId = null;
          isPolling = false;
          progressText = null;
        });
      } else {
        setState(() {
          videoUrl = 'Cancel failed: ${res["error"] ?? "Unknown error"}';
          progressText = null;
        });
      }
    } catch (e) {
      setState(() {
        videoUrl = 'Error canceling task: $e';
        progressText = null;
      });
    } finally {
      setState(() {
        isCanceling = false;
      });
    }
  }

  Map<String, dynamic> _safeDecode(String? body) {
    if (body == null || body.isEmpty) return {};
    try {
      final decoded = jsonDecode(body);
      if (decoded is Map<String, dynamic>) return decoded;
      return {"data": decoded};
    } catch (_) {
      return {"raw": body};
    }
  }

  String? _extractProgress(dynamic task) {
    // Try common fields returned by task objects to show a friendly progress
    if (task is Map<String, dynamic>) {
      // e.g., task["progress"] might be 0..100 or 0..1
      final p = task["progress"];
      if (p is num) {
        if (p <= 1.0) {
          return "${(p * 100).clamp(0, 100).toStringAsFixed(1)}%";
        } else {
          return "${p.clamp(0, 100)}%";
        }
      }
      // Sometimes SDKs expose nested status/progress
      final meta = task["metadata"];
      if (meta is Map && meta["progress"] is num) {
        final mp = meta["progress"] as num;
        if (mp <= 1.0) {
          return "${(mp * 100).clamp(0, 100).toStringAsFixed(1)}%";
        } else {
          return "${mp.clamp(0, 100)}%";
        }
      }
    }
    return null; // fallback to textual status
  }

  @override
  void dispose() {
    _videoPlayerController?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    // Note: In general, don’t embed a MaterialApp inside a screen; use Scaffold under your app’s MaterialApp.
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        appBar: AppBar(title: const Text('RunwayML Generator')),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            spacing: 20,
            children: [
              isGenerating
                  ? const Text('Generating Video...')
                  : ElevatedButton(
                onPressed: generateVideo,
                child: const Text('Generate Video'),
              ),
              if (taskId != null && (isLoading || isPolling))
                isCanceling
                    ? const Text('Task is being canceled...')
                    : ElevatedButton(
                  onPressed: cancelTask,
                  child: const Text('Cancel Task'),
                ),
              if (progressText != null) Text(progressText!),
              (isLoading || isPolling)
                  ? const CircularProgressIndicator()
                  : videoUrl != null
                  ? (_videoPlayerController != null &&
                  _videoPlayerController!.value.isInitialized)
                  ? AspectRatio(
                aspectRatio:
                _videoPlayerController!.value.aspectRatio,
                child: VideoPlayer(_videoPlayerController!),
              )
                  : Text(videoUrl ?? '')
                  : const SizedBox(),
            ],
          ),
        ),
      ),
    );
  }
}