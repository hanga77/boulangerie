/* scripts.js — remplace bootstrap.bundle.min.js pour fonctionner sans CDN */

document.addEventListener('DOMContentLoaded', function () {

    /* ── Navbar toggle (hamburger mobile) ──────────────────────────────── */
    const toggler = document.querySelector('.navbar-toggler');
    if (toggler) {
        toggler.addEventListener('click', function () {
            const targetId = this.getAttribute('data-bs-target');
            const target = document.querySelector(targetId);
            if (target) target.classList.toggle('show');
        });
    }

    /* ── Fermeture des alertes flash ───────────────────────────────────── */
    document.querySelectorAll('[data-bs-dismiss="alert"]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const alert = this.closest('.alert');
            if (alert) {
                alert.style.opacity = '0';
                alert.style.transition = 'opacity 0.3s';
                setTimeout(() => alert.remove(), 300);
            }
        });
    });

    /* ── Dropdowns Bootstrap (navbar user menu etc.) ────────────────────── */
    document.querySelectorAll('[data-bs-toggle="dropdown"]').forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            e.stopPropagation();
            const menu = this.nextElementSibling;
            if (menu && menu.classList.contains('dropdown-menu')) {
                const isOpen = menu.classList.contains('show');
                // Ferme tous les autres dropdowns
                document.querySelectorAll('.dropdown-menu.show').forEach(m => m.classList.remove('show'));
                document.querySelectorAll('.dropdown-toggle.show').forEach(t => {
                    t.classList.remove('show');
                    t.setAttribute('aria-expanded', 'false');
                });
                if (!isOpen) {
                    menu.classList.add('show');
                    this.classList.add('show');
                    this.setAttribute('aria-expanded', 'true');
                }
            }
        });
    });

    /* Ferme les dropdowns en cliquant ailleurs */
    document.addEventListener('click', function () {
        document.querySelectorAll('.dropdown-menu.show').forEach(m => m.classList.remove('show'));
        document.querySelectorAll('[data-bs-toggle="dropdown"].show').forEach(t => {
            t.classList.remove('show');
            t.setAttribute('aria-expanded', 'false');
        });
    });

    /* ── Confirmation sur les boutons de suppression ────────────────────── */
    document.querySelectorAll('form[onsubmit]').forEach(function (form) {
        const onsubmit = form.getAttribute('onsubmit');
        if (onsubmit && onsubmit.includes('confirm')) {
            form.removeAttribute('onsubmit');
            form.addEventListener('submit', function (e) {
                if (!confirm('Confirmer la suppression ?')) {
                    e.preventDefault();
                }
            });
        }
    });
});
