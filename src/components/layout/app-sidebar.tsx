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
    <aside className="w-64 min-h-screen bg-gray-900 text-gray-100 flex flex-col">
      {/* Header */}
      <div className="p-4 border-b border-gray-700">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 bg-red-500 rounded-lg flex items-center justify-center flex-shrink-0">
            <Inbox className="w-4 h-4 text-white" />
          </div>
          <span className="font-semibold text-white">Mijn Taken</span>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 p-3 space-y-1 overflow-y-auto">
        {navItems.map(({ href, icon: Icon, label }) => (
          <Link
            key={href}
            href={href}
            className={cn(
              "flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors",
              pathname === href || pathname.startsWith(href + "/")
                ? "bg-gray-700 text-white"
                : "text-gray-300 hover:bg-gray-800 hover:text-white"
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
            "flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors",
            pathname === "/ai-suggestions"
              ? "bg-gray-700 text-white"
              : "text-gray-300 hover:bg-gray-800 hover:text-white"
          )}
        >
          <Sparkles className="w-4 h-4 flex-shrink-0 text-yellow-400" />
          AI Suggesties
        </Link>

        {/* Projects */}
        <div className="pt-4">
          <button
            onClick={() => setProjectsOpen(!projectsOpen)}
            className="flex items-center justify-between w-full px-3 py-1 text-xs font-semibold text-gray-400 uppercase tracking-wider hover:text-gray-200 transition-colors"
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
                    "flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors group",
                    pathname === `/projects/${project.id}`
                      ? "bg-gray-700 text-white"
                      : "text-gray-300 hover:bg-gray-800 hover:text-white"
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
                className="flex items-center gap-3 px-3 py-2 rounded-lg text-sm text-gray-400 hover:bg-gray-800 hover:text-gray-200 transition-colors"
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
              "flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors",
              pathname === "/labels"
                ? "bg-gray-700 text-white"
                : "text-gray-300 hover:bg-gray-800 hover:text-white"
            )}
          >
            <Hash className="w-4 h-4 flex-shrink-0" />
            Labels
          </Link>
        </div>
      </nav>

      {/* Footer */}
      <div className="p-3 border-t border-gray-700 space-y-1">
        <Link
          href="/settings/integrations"
          className={cn(
            "flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors",
            pathname.startsWith("/settings")
              ? "bg-gray-700 text-white"
              : "text-gray-300 hover:bg-gray-800 hover:text-white"
          )}
        >
          <Settings className="w-4 h-4" />
          Instellingen
        </Link>
        {session?.user && (
          <button
            onClick={() => signOut({ callbackUrl: "/inbox" })}
            className="flex items-center gap-3 px-3 py-2 rounded-lg text-sm text-gray-300 hover:bg-gray-800 hover:text-white transition-colors w-full text-left"
          >
            <LogOut className="w-4 h-4" />
            Uitloggen
          </button>
        )}
      </div>
    </aside>
  );
}
