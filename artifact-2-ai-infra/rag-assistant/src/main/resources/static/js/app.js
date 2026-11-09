/* C2 TL Artifact 2 — RAG Assistant UI interactivity */
document.addEventListener("DOMContentLoaded", () => {
  const qa = document.getElementById("question");
  if (qa && !qa.value) {
    qa.value = "How does the auth service handle token refresh?";
  }
});

function copyToClipboard(id) {
  const el = document.getElementById(id);
  if (!el) return;
  navigator.clipboard.writeText(el.innerText).then(() => {
    const btn = el.closest(".result-card")?.querySelector("button");
    const original = btn ? btn.innerText : "";
    if (btn) { btn.innerText = "Copied!"; setTimeout(() => btn.innerText = original, 1200); }
  });
}
