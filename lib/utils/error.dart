import 'package:flutter/material.dart';

void showErrorDialog(String message, context) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('Error'),
          content: Text(message),
          actions: <Widget>[
            TextButton(
              child: const Text('Ok'),
              onPressed: () {
                Navigator.of(context).pop();
              },
            )
          ],
        );
      },
    );
  }
  Future showErrorDialogFuture(String message, BuildContext context, {VoidCallback? onDismiss}) async { await showDialog( context: context, builder: (BuildContext ctx) { return AlertDialog( title: const Text('Error'), content: Text(message), actions: [ TextButton( child: const Text('Ok'), onPressed: () { Navigator.of(ctx).pop(); }, ), ], ); }, ); if (onDismiss != null) onDismiss(); }