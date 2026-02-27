"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Flag, Calendar, Tag, FolderOpen, X } from "lucide-react";
import { cn } from "@/lib/utils";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import type { TaskWithRelations } from "@/types";

interface TaskFormProps {
  projectId?: string;
  sectionId?: string;
  onSuccess?: () => void;
  onCancel?: () => void;
  task?: TaskWithRelations;
  queryKey?: string[];
}

const PRIORITIES = [
  { value: 1, label: "Urgent", color: "text-red-500" },
  { value: 2, label: "Hoog", color: "text-orange-400" },
  { value: 3, label: "Normaal", color: "text-blue-500" },
  { value: 4, label: "Geen", color: "text-gray-400" },
];

export function TaskForm({
  projectId,
  sectionId,
  onSuccess,
  onCancel,
  task,
  queryKey = ["tasks"],
}: TaskFormProps) {
  const isEditing = !!task;
  const [title, setTitle] = useState(task?.title ?? "");
  const [description, setDescription] = useState(task?.description ?? "");
  const [priority, setPriority] = useState(task?.priority ?? 4);
  const [dueDate, setDueDate] = useState(
    task?.dueDate ? new Date(task.dueDate).toISOString().split("T")[0] : ""
  );
  const [showPriority, setShowPriority] = useState(false);

  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: async (data: Record<string, unknown>) => {
      const url = isEditing ? `/api/tasks/${task!.id}` : "/api/tasks";
      const method = isEditing ? "PATCH" : "POST";
      const res = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });
      if (!res.ok) throw new Error("Fout bij opslaan van taak");
      return res.json();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey });
      toast.success(isEditing ? "Taak bijgewerkt" : "Taak aangemaakt");
      onSuccess?.();
    },
    onError: () => {
      toast.error("Er ging iets mis. Probeer opnieuw.");
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) return;

    mutation.mutate({
      title: title.trim(),
      description: description.trim() || undefined,
      priority,
      dueDate: dueDate || undefined,
      projectId: projectId || undefined,
      sectionId: sectionId || undefined,
    });
  };

  return (
    <form onSubmit={handleSubmit} className="border border-gray-200 rounded-lg p-4 bg-white">
      <Input
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        placeholder="Taaknaam"
        className="border-0 shadow-none text-base font-medium px-0 focus-visible:ring-0 placeholder:text-gray-400"
        autoFocus
      />
      <Input
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        placeholder="Beschrijving"
        className="border-0 shadow-none text-sm px-0 mt-1 focus-visible:ring-0 placeholder:text-gray-300"
      />

      {/* Toolbar */}
      <div className="flex items-center gap-2 mt-3 pt-3 border-t border-gray-100">
        {/* Due date */}
        <div className="flex items-center gap-1.5">
          <Calendar className="w-4 h-4 text-gray-400" />
          <input
            type="date"
            value={dueDate}
            onChange={(e) => setDueDate(e.target.value)}
            className="text-xs text-gray-500 border-0 outline-none bg-transparent cursor-pointer"
          />
        </div>

        {/* Priority picker */}
        <div className="relative">
          <button
            type="button"
            onClick={() => setShowPriority(!showPriority)}
            className={cn(
              "flex items-center gap-1 text-xs px-2 py-1 rounded hover:bg-gray-100",
              priority < 4
                ? PRIORITIES.find((p) => p.value === priority)?.color
                : "text-gray-400"
            )}
          >
            <Flag className="w-3.5 h-3.5" />
            {priority < 4 ? PRIORITIES.find((p) => p.value === priority)?.label : "Prioriteit"}
          </button>
          {showPriority && (
            <div className="absolute top-full left-0 mt-1 bg-white border border-gray-200 rounded-lg shadow-lg z-10 py-1 min-w-[120px]">
              {PRIORITIES.map((p) => (
                <button
                  key={p.value}
                  type="button"
                  onClick={() => {
                    setPriority(p.value);
                    setShowPriority(false);
                  }}
                  className={cn(
                    "flex items-center gap-2 w-full px-3 py-1.5 text-sm hover:bg-gray-50",
                    p.color
                  )}
                >
                  <Flag className="w-3.5 h-3.5" />
                  {p.label}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Spacer */}
        <div className="flex-1" />

        {/* Actions */}
        <Button
          type="button"
          variant="ghost"
          size="sm"
          onClick={onCancel}
          className="text-gray-500"
        >
          <X className="w-4 h-4" />
        </Button>
        <Button
          type="submit"
          size="sm"
          disabled={!title.trim() || mutation.isPending}
        >
          {isEditing ? "Opslaan" : "Toevoegen"}
        </Button>
      </div>
    </form>
  );
}
