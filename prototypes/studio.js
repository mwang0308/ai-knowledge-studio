document.querySelectorAll('input[type="date"]').forEach((input) => {
  let openedAt = 0;

  function openPicker() {
    if (typeof input.showPicker !== "function") return;
    const now = Date.now();
    if (now - openedAt < 300) return;
    openedAt = now;
    input.showPicker();
  }

  input.addEventListener("click", openPicker);
  input.addEventListener("focus", openPicker);
});
