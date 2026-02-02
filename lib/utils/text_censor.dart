class TextSafetyResult {
  final bool ok;
  final String cleaned;
  final String? reason;
  TextSafetyResult({required this.ok, required this.cleaned, this.reason});
}

class TextSafety {
  // Minimal example — extend this list for your use case and locales
  static final Set<String> bannedWords = {
    // Sexual content
    'nude','porn','xxx','sex','nsfw',
    // Hate/offensive slurs (example placeholders; curate this responsibly)
    'slur1','slur2','slur3',
    // Self-harm / violence extremes (tune per policy)
    'suicide','execute','behead',
  };

  static final List<RegExp> bannedPatterns = [
    RegExp(r'https?:\/\/|www\.', caseSensitive: false), // links
    RegExp(r'(@\w{2,}|#\w{2,})', caseSensitive: false), // mentions/hashtags if you want to block
  ];

  static String normalize(String s) {
    // Lowercase + collapse repeated chars (e.g., “seeeexxx” -> “sex”)
    final lower = s.toLowerCase();
    return lower.replaceAll(RegExp(r'(.)\1{2,}'), r'\$1\$1'); // limit repeats
  }

  static TextSafetyResult check(String text, {int maxLen = 200}) {
    final original = text.trim();
    if (original.isEmpty) {
      return TextSafetyResult(ok: false, cleaned: '', reason: 'Prompt is empty.');
    }
    if (original.length > maxLen) {
      return TextSafetyResult(ok: false, cleaned: '', reason: 'Prompt too long.');
    }

    final norm = normalize(original);

    for (final rx in bannedPatterns) {
      if (rx.hasMatch(norm)) {
        return TextSafetyResult(ok: false, cleaned: '', reason: 'Links or disallowed patterns are not allowed.');
      }
    }

    // Token check
    final tokens = norm.split(RegExp(r'\s+'));
    for (final t in tokens) {
      if (bannedWords.contains(t)) {
        return TextSafetyResult(ok: false, cleaned: '', reason: 'Inappropriate terms detected.');
      }
    }

    // If you want to soften content, prepend a safety style
    final safeWrapped = 'In a wholesome, family-friendly, non-violent style with no nudity, gore, or hate speech: $original';
    return TextSafetyResult(ok: true, cleaned: safeWrapped);
  }
}