"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { signOut, useSession } from "next-auth/react";
import {
  Inbox,
  Calendar,
  CalendarDays,
  Hash,
  Plus,
  Settings,
  LogOut,
  ChevronDown,
  ChevronRight,
  Sparkles,
} from "lucide-react";
import { useState } from "react";
import { cn } from "@/lib/utils";
import { useQuery } from "@tanstack/react-query";
import type { ProjectWithSections } from "@/types";
import { ThemeSelector } from "@/components/layout/theme-selector";

const navItems = [
  { href: "/inbox", icon: Inbox, label: "Inbox" },
  { href: "/today", icon: Calendar, label: "Vandaag" },
  { href: "/upcoming", icon: CalendarDays, label: "Aankomend" },
];

export function AppSidebar() {
  const pathname = usePathname();
  const [projectsOpen, setProjectsOpen] = useState(true);
  const { data: session } = useSession();

  const { data: projects = [] } = useQuery<ProjectWithSections[]>({
    queryKey: ["projects"],
    queryFn: () => fetch("/api/projects").then((r) => r.json()),
  });

  return (
    <aside className="sidebar-root w-64 min-h-screen flex flex-col">
      {/* Header */}
      <div className="p-4 border-b sidebar-border">
        <div className="flex items-center gap-2">
          <div
            className="w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0"
            style={{ backgroundColor: "var(--primary)" }}
          >
            <Inbox className="w-4 h-4" style={{ color: "var(--primary-foreground)" }} />
          </div>
          <span className="font-semibold" style={{ color: "var(--sidebar-foreground)" }}>
            Mijn Taken
          </span>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 p-3 space-y-1 overflow-y-auto">
        {navItems.map(({ href, icon: Icon, label }) => (
          <Link
            key={href}
            href={href}
            className={cn(
              "sidebar-item",
              (pathname === href || pathname.startsWith(href + "/")) && "sidebar-item-active"
            )}
          >
            <Icon className="w-4 h-4 flex-shrink-0" />
            {label}
          </Link>
        ))}

        {/* AI Suggesties */}
        <Link
          href="/ai-suggestions"
          className={cn(
            "sidebar-item",
            pathname === "/ai-suggestions" && "sidebar-item-active"
          )}
        >
          <Sparkles className="w-4 h-4 flex-shrink-0 text-yellow-400" />
          AI Suggesties
        </Link>

        {/* Projects */}
        <div className="pt-4">
          <button
            onClick={() => setProjectsOpen(!projectsOpen)}
            className="sidebar-section-label flex items-center justify-between w-full px-3 py-1 text-xs font-semibold uppercase tracking-wider transition-colors"
          >
            <span>Projecten</span>
            {projectsOpen ? (
              <ChevronDown className="w-3 h-3" />
            ) : (
              <ChevronRight className="w-3 h-3" />
            )}
          </button>

          {projectsOpen && (
            <div className="mt-1 space-y-0.5">
              {projects.map((project) => (
                <Link
                  key={project.id}
                  href={`/projects/${project.id}`}
                  className={cn(
                    "sidebar-item",
                    pathname === `/projects/${project.id}` && "sidebar-item-active"
                  )}
                >
                  <span
                    className="w-2.5 h-2.5 rounded-full flex-shrink-0"
                    style={{ backgroundColor: project.color }}
                  />
                  <span className="truncate flex-1">{project.name}</span>
                </Link>
              ))}
              <Link
                href="/projects/new"
                className="sidebar-item"
              >
                <Plus className="w-4 h-4" />
                Project toevoegen
              </Link>
            </div>
          )}
        </div>

        {/* Labels */}
        <div className="pt-2">
          <Link
            href="/labels"
            className={cn(
              "sidebar-item",
              pathname === "/labels" && "sidebar-item-active"
            )}
          >
            <Hash className="w-4 h-4 flex-shrink-0" />
            Labels
          </Link>
        </div>
      </nav>

      {/* Footer */}
      <div className="p-3 border-t sidebar-border space-y-1">
        <ThemeSelector />
        <Link
          href="/settings/integrations"
          className={cn(
            "sidebar-item",
            pathname.startsWith("/settings") && "sidebar-item-active"
          )}
        >
          <Settings className="w-4 h-4" />
          Instellingen
        </Link>
        {session?.user && (
          <button
            onClick={() => signOut({ callbackUrl: "/inbox" })}
            className="sidebar-item w-full"
          >
            <LogOut className="w-4 h-4" />
            Uitloggen
          </button>
        )}
      </div>
    </aside>
  );
}
