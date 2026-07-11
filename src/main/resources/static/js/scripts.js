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

    /* ── Toasts flash (succès/avertissement/erreur) ──────────────────────── */
    function dismissToast(toast) {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 200);
    }

    document.querySelectorAll('.auto-toast').forEach(function (toast) {
        toast.classList.add('show');
        const delay = toast.classList.contains('text-bg-danger') ? 6000 : 4000;
        const timer = setTimeout(() => dismissToast(toast), delay);
        toast.querySelectorAll('[data-toast-dismiss]').forEach(function (btn) {
            btn.addEventListener('click', function () {
                clearTimeout(timer);
                dismissToast(toast);
            });
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

    /* ── Modals Bootstrap ───────────────────────────────────────────────── */
    function openModal(targetSelector) {
        const modal = document.querySelector(targetSelector);
        if (!modal) return;
        let backdrop = document.getElementById('_bs-backdrop');
        if (!backdrop) {
            backdrop = document.createElement('div');
            backdrop.id = '_bs-backdrop';
            backdrop.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,.5);z-index:1040;';
            document.body.appendChild(backdrop);
        }
        backdrop.onclick = function () { closeModal(modal); };
        modal.style.display = 'block';
        modal.classList.add('show');
        document.body.style.overflow = 'hidden';
    }

    function closeModal(modal) {
        modal.style.display = 'none';
        modal.classList.remove('show');
        document.body.style.overflow = '';
        const backdrop = document.getElementById('_bs-backdrop');
        if (backdrop) backdrop.remove();
    }

    document.querySelectorAll('[data-bs-toggle="modal"]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const target = this.getAttribute('data-bs-target');
            if (target) openModal(target);
        });
    });

    document.querySelectorAll('[data-bs-dismiss="modal"]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const modal = this.closest('.modal');
            if (modal) closeModal(modal);
        });
    });
});
