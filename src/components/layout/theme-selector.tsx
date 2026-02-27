"use client";

import { useState } from "react";
import { Palette, Check } from "lucide-react";
import { THEMES } from "@/lib/theme";
import { useTheme } from "@/components/layout/providers";

export function ThemeSelector() {
  const { theme, setTheme } = useTheme();
  const [open, setOpen] = useState(false);

  return (
    <div className="relative">
      <button
        onClick={() => setOpen((o) => !o)}
        className="sidebar-item w-full"
        title="Thema kiezen"
      >
        <Palette className="w-4 h-4 flex-shrink-0" />
        <span>Thema</span>
      </button>

      {open && (
        <>
          {/* Backdrop */}
          <div
            className="fixed inset-0 z-40"
            onClick={() => setOpen(false)}
          />

          {/* Panel */}
          <div
            className="absolute bottom-full left-0 mb-2 z-50 rounded-xl shadow-2xl border border-white/10 p-3 w-64"
            style={{ backgroundColor: "var(--sidebar)", borderColor: "var(--sidebar-border)" }}
          >
            <p
              className="text-xs font-semibold uppercase tracking-wider mb-2 px-1"
              style={{ color: "color-mix(in srgb, var(--sidebar-foreground) 50%, transparent)" }}
            >
              Kies een thema
            </p>
            <div className="grid grid-cols-1 gap-1">
              {THEMES.map((t) => (
                <button
                  key={t.id}
                  onClick={() => {
                    setTheme(t.id);
                    setOpen(false);
                  }}
                  className="flex items-center gap-3 px-2 py-2 rounded-lg text-sm transition-colors text-left w-full"
                  style={{
                    color: "var(--sidebar-foreground)",
                    backgroundColor:
                      theme === t.id
                        ? "var(--sidebar-active)"
                        : "transparent",
                  }}
                  onMouseEnter={(e) => {
                    if (theme !== t.id)
                      (e.currentTarget as HTMLButtonElement).style.backgroundColor =
                        "var(--sidebar-hover)";
                  }}
                  onMouseLeave={(e) => {
                    if (theme !== t.id)
                      (e.currentTarget as HTMLButtonElement).style.backgroundColor =
                        "transparent";
                  }}
                >
                  {/* Color swatch */}
                  <span
                    className="w-5 h-5 rounded-full flex-shrink-0 ring-2 ring-white/20"
                    style={{ backgroundColor: t.primary }}
                  />
                  <span className="flex-1">{t.name}</span>
                  {theme === t.id && (
                    <Check className="w-4 h-4 flex-shrink-0" style={{ color: t.primary }} />
                  )}
                </button>
              ))}
            </div>
          </div>
        </>
      )}
    </div>
  );
}
