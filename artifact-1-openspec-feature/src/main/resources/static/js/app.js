/* C2 TL Artifact 1 — UI interactivity */
document.addEventListener("DOMContentLoaded", () => {
  // Collapsible step panels
  document.querySelectorAll(".step-header").forEach((header) => {
    header.addEventListener("click", () => {
      const card = header.closest(".step-card");
      const body = card.querySelector(".step-body");
      const isOpen = body.classList.contains("open");
      // Close all panels in this card set
      card.querySelectorAll(".step-body.open").forEach((b) => b.classList.remove("open"));
      if (!isOpen) body.classList.add("open");
    });
  });

  // Auto-fill sample diff on demo page
  const diffTextarea = document.getElementById("diff");
  if (diffTextarea && !diffTextarea.value) {
    diffTextarea.value = `diff --git a/src/auth.py b/src/auth.py
new file mode 100644
--- /dev/null
+++ b/src/auth.py
@@ -0,0 +1,5 @@
+password = "hardcoded_secret_123"
+def login(user):
+    for i in range(len(users)):
+        if users[i] == user:
+            return True
+    return False`;
  }
});

function copyToClipboard(id) {
  const el = document.getElementById(id);
  if (!el) return;
  navigator.clipboard.writeText(el.innerText).then(() => {
    const btn = el.closest(".code-block")?.previousElementSibling;
    const original = btn ? btn.innerText : "";
    if (btn) { btn.innerText = "Copied!"; setTimeout(() => btn.innerText = original, 1200); }
  });
}
