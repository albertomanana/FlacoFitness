(() => {
    const REDUCE_MOTION = window.matchMedia("(prefers-reduced-motion: reduce)");

    document.addEventListener("DOMContentLoaded", () => {
        document.documentElement.classList.add("ff-hud-motion-ready");

        if (REDUCE_MOTION.matches || !getAnimeRunner()) {
            document.documentElement.classList.add("ff-hud-motion-css");
            return;
        }

        revealPanels();
        primeHudHover();
        animateOperationalChips();
    });

    function revealPanels() {
        const targets = document.querySelectorAll(
            ".ff-command-stack > .ff-section-header, .ff-command-stack > .ff-command-hero, .ff-command-stack > section, .ff-command-stack > .ff-surface-card, .ff-command-stack .ff-hud-card"
        );

        if (!targets.length) {
            return;
        }

        runAnimation(targets, {
            opacity: [0, 1],
            translateY: [16, 0],
            scale: [0.985, 1],
            delay: stagger(55),
            duration: 620,
            easing: "easeOutCubic"
        });
    }

    function primeHudHover() {
        document.querySelectorAll(".ff-hud-card, .ff-kpi-card, .ff-dashboard-alert-card").forEach((card) => {
            card.addEventListener("mouseenter", () => {
                removeAnimation(card);
                runAnimation(card, {
                    translateY: -3,
                    duration: 220,
                    easing: "easeOutCubic"
                });
            });

            card.addEventListener("mouseleave", () => {
                removeAnimation(card);
                runAnimation(card, {
                    translateY: 0,
                    duration: 260,
                    easing: "easeOutCubic"
                });
            });
        });
    }

    function animateOperationalChips() {
        const chips = document.querySelectorAll(".ff-operational-clock, .ff-topbar-btn, .ff-topbar-avatar-btn");
        if (!chips.length) {
            return;
        }

        runAnimation(chips, {
            opacity: [0.82, 1],
            translateY: [-4, 0],
            delay: stagger(35),
            duration: 420,
            easing: "easeOutCubic"
        });
    }

    function getAnimeRunner() {
        if (!window.anime) {
            return null;
        }

        if (typeof window.anime === "function") {
            return (targets, params) => window.anime({ targets, ...params });
        }

        if (typeof window.anime.animate === "function") {
            return (targets, params) => window.anime.animate(targets, normalizeParams(params));
        }

        return null;
    }

    function runAnimation(targets, params) {
        const runner = getAnimeRunner();
        if (!runner) {
            return null;
        }

        try {
            return runner(targets, params);
        } catch (error) {
            document.documentElement.classList.add("ff-hud-motion-css");
            return null;
        }
    }

    function normalizeParams(params) {
        const nextParams = { ...params };
        if (nextParams.easing) {
            nextParams.ease = nextParams.easing === "easeInOutSine" ? "inOut(2)" : "out(3)";
            delete nextParams.easing;
        }
        return nextParams;
    }

    function removeAnimation(targets) {
        if (window.anime && typeof window.anime.remove === "function") {
            window.anime.remove(targets);
        }
    }

    function stagger(amount, options = {}) {
        if (window.anime && typeof window.anime.stagger === "function") {
            return window.anime.stagger(amount, options);
        }

        return (_target, index) => (options.start || 0) + (index * amount);
    }
})();
