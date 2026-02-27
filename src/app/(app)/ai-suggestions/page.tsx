"use client";

import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Sparkles, Loader2, Check, X, Mail, RefreshCw } from "lucide-react";
import { toast } from "sonner";
import { cn, PRIORITY_COLORS, PRIORITY_LABELS } from "@/lib/utils";
import type { AISuggestedTask } from "@/types";

interface SuggestionResult {
  suggestionId: string;
  suggestions: AISuggestedTask[];
}

export default function AISuggestionsPage() {
  const [emailText, setEmailText] = useState("");
  const [suggestions, setSuggestions] = useState<AISuggestedTask[]>([]);
  const [suggestionId, setSuggestionId] = useState<string | null>(null);
  const [selected, setSelected] = useState<Set<number>>(new Set());
  const [source, setSource] = useState<"gmail" | "outlook" | "manual">("manual");

  const analyzeMutation = useMutation<SuggestionResult, Error, void>({
    mutationFn: async () => {
      const res = await fetch("/api/ai/suggest-tasks", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          type: "text",
          text: emailText,
        }),
      });
      if (!res.ok) throw new Error("AI analyse mislukt");
      return res.json();
    },
    onSuccess: (data) => {
      setSuggestions(data.suggestions);
      setSuggestionId(data.suggestionId);
      setSelected(new Set(data.suggestions.map((_: AISuggestedTask, i: number) => i)));
    },
    onError: () => {
      toast.error("AI analyse mislukt. Controleer je Anthropic API key.");
    },
  });

  const gmailMutation = useMutation<{ suggestions: AISuggestedTask[]; suggestionId: string; emailSubject: string }[], Error, void>({
    mutationFn: async () => {
      // Fetch emails first
      const emailRes = await fetch("/api/integrations/google/gmail");
      if (!emailRes.ok) throw new Error("Gmail ophalen mislukt. Controleer je Google verbinding.");
      const emails = await emailRes.json();

      if (!emails.length) return [];

      // Process first email for demo
      const results = [];
      for (const email of emails.slice(0, 3)) {
        const res = await fetch("/api/ai/suggest-tasks", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            type: "email",
            text: email.body || email.snippet,
            emailMeta: {
              id: email.id,
              from: email.from,
              subject: email.subject,
              date: email.date,
              provider: "google",
            },
          }),
        });
        if (res.ok) {
          const data = await res.json();
          if (data.suggestions.length > 0) {
            results.push({ ...data, emailSubject: email.subject });
          }
        }
      }
      return results;
    },
    onSuccess: (results) => {
      if (!results.length) {
        toast.info("Geen nieuwe emails gevonden voor analyse.");
        return;
      }
      // Take first result with suggestions
      const first = results[0];
      setSuggestions(first.suggestions);
      setSuggestionId(first.suggestionId);
      setSelected(new Set(first.suggestions.map((_: AISuggestedTask, i: number) => i)));
      toast.success(`${results.reduce((a, r) => a + r.suggestions.length, 0)} suggesties gegenereerd uit ${results.length} emails`);
    },
    onError: (error) => {
      toast.error(error.message);
    },
  });

  const acceptMutation = useMutation({
    mutationFn: async () => {
      if (!suggestionId) throw new Error("Geen suggestie ID");
      const res = await fetch("/api/ai/parse-email", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          suggestionId,
          taskIndices: Array.from(selected),
        }),
      });
      if (!res.ok) throw new Error("Taken aanmaken mislukt");
      return res.json();
    },
    onSuccess: (data) => {
      toast.success(`${data.created} taken aangemaakt!`);
      setSuggestions([]);
      setSuggestionId(null);
      setEmailText("");
      setSelected(new Set());
    },
    onError: () => {
      toast.error("Fout bij aanmaken taken.");
    },
  });

  const toggleSelect = (index: number) => {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(index)) next.delete(index);
      else next.add(index);
      return next;
    });
  };

  return (
    <div className="flex-1 p-6 max-w-3xl mx-auto w-full">
      <div className="flex items-center gap-3 mb-2">
        <Sparkles className="w-6 h-6 text-yellow-500" />
        <h1 className="text-2xl font-bold text-gray-900">AI Taaksuggesties</h1>
      </div>
      <p className="text-sm text-gray-500 mb-6">
        Plak een email of tekst en Claude genereert automatisch actiepunten voor je.
      </p>

      {/* Source buttons */}
      <div className="flex gap-2 mb-4">
        <Button
          variant={source === "gmail" ? "default" : "outline"}
          size="sm"
          onClick={() => setSource("gmail")}
          className="gap-2"
        >
          <Mail className="w-4 h-4" />
          Gmail
        </Button>
        <Button
          variant={source === "manual" ? "default" : "outline"}
          size="sm"
          onClick={() => setSource("manual")}
        >
          Tekst plakken
        </Button>
      </div>

      {source === "gmail" && (
        <div className="mb-6 p-4 bg-blue-50 rounded-xl border border-blue-100">
          <p className="text-sm text-blue-700 mb-3">
            Haal de laatste ongelezen emails op uit Gmail en analyseer ze automatisch.
          </p>
          <Button
            onClick={() => gmailMutation.mutate()}
            disabled={gmailMutation.isPending}
            className="gap-2"
          >
            {gmailMutation.isPending ? (
              <Loader2 className="w-4 h-4 animate-spin" />
            ) : (
              <RefreshCw className="w-4 h-4" />
            )}
            Gmail emails analyseren
          </Button>
        </div>
      )}

      {source === "manual" && (
        <div className="mb-6">
          <textarea
            value={emailText}
            onChange={(e) => setEmailText(e.target.value)}
            placeholder="Plak hier een email of tekst..."
            className="w-full h-48 p-4 border border-gray-200 rounded-xl text-sm resize-none focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-transparent"
          />
          <Button
            onClick={() => analyzeMutation.mutate()}
            disabled={!emailText.trim() || analyzeMutation.isPending}
            className="mt-3 gap-2"
          >
            {analyzeMutation.isPending ? (
              <Loader2 className="w-4 h-4 animate-spin" />
            ) : (
              <Sparkles className="w-4 h-4" />
            )}
            Analyseren met Claude AI
          </Button>
        </div>
      )}

      {/* Suggestions */}
      {suggestions.length > 0 && (
        <div>
          <div className="flex items-center justify-between mb-3">
            <h2 className="font-semibold text-gray-800">
              {suggestions.length} suggesties gevonden
            </h2>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setSelected(new Set(suggestions.map((_, i) => i)))}
              >
                Alles selecteren
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setSelected(new Set())}
              >
                Deselecteer alles
              </Button>
            </div>
          </div>

          <div className="space-y-2 mb-4">
            {suggestions.map((task, i) => (
              <div
                key={i}
                onClick={() => toggleSelect(i)}
                className={cn(
                  "flex items-start gap-3 p-4 rounded-xl border cursor-pointer transition-colors",
                  selected.has(i)
                    ? "bg-red-50 border-red-200"
                    : "bg-white border-gray-200 opacity-60"
                )}
              >
                <div
                  className={cn(
                    "w-5 h-5 rounded-full border-2 flex items-center justify-center flex-shrink-0 mt-0.5",
                    selected.has(i)
                      ? "bg-red-500 border-red-500"
                      : "border-gray-300"
                  )}
                >
                  {selected.has(i) && <Check className="w-3 h-3 text-white" />}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-900">{task.title}</p>
                  {task.description && (
                    <p className="text-xs text-gray-500 mt-0.5">{task.description}</p>
                  )}
                  <div className="flex items-center gap-3 mt-1.5">
                    {task.priority < 4 && (
                      <span
                        className={cn(
                          "text-xs font-medium",
                          PRIORITY_COLORS[task.priority]
                        )}
                      >
                        {PRIORITY_LABELS[task.priority]}
                      </span>
                    )}
                    {task.dueDate && (
                      <span className="text-xs text-gray-400">
                        Deadline: {new Date(task.dueDate).toLocaleDateString("nl-NL")}
                      </span>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>

          <div className="flex gap-3">
            <Button
              onClick={() => acceptMutation.mutate()}
              disabled={selected.size === 0 || acceptMutation.isPending}
              className="gap-2"
            >
              {acceptMutation.isPending ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <Check className="w-4 h-4" />
              )}
              {selected.size} taak{selected.size !== 1 ? "en" : ""} aanmaken
            </Button>
            <Button
              variant="outline"
              onClick={() => {
                setSuggestions([]);
                setSuggestionId(null);
                setSelected(new Set());
              }}
              className="gap-2"
            >
              <X className="w-4 h-4" />
              Verwerpen
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
