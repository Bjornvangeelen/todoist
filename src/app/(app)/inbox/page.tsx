"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { TaskItem } from "@/components/tasks/task-item";
import { TaskForm } from "@/components/tasks/task-form";
import { Button } from "@/components/ui/button";
import { Plus, Inbox } from "lucide-react";
import type { TaskWithRelations } from "@/types";

export default function InboxPage() {
  const [showForm, setShowForm] = useState(false);

  const { data: tasks = [], isLoading } = useQuery<TaskWithRelations[]>({
    queryKey: ["tasks", "inbox"],
    queryFn: () => fetch("/api/tasks?filter=inbox").then((r) => r.json()),
  });

  return (
    <div className="flex-1 p-6 max-w-3xl mx-auto w-full">
      <div className="flex items-center gap-3 mb-6">
        <Inbox className="w-6 h-6 text-gray-700" />
        <h1 className="text-2xl font-bold text-gray-900">Inbox</h1>
        {tasks.length > 0 && (
          <span className="text-sm text-gray-400">({tasks.length})</span>
        )}
      </div>

      {/* Task list */}
      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
        {isLoading ? (
          <div className="p-8 text-center text-gray-400 text-sm">Laden...</div>
        ) : tasks.length === 0 && !showForm ? (
          <div className="p-12 text-center">
            <Inbox className="w-12 h-12 text-gray-200 mx-auto mb-3" />
            <p className="text-gray-400 text-sm">Je inbox is leeg!</p>
            <p className="text-gray-300 text-xs mt-1">
              Taken zonder project verschijnen hier
            </p>
          </div>
        ) : (
          tasks.map((task) => (
            <TaskItem
              key={task.id}
              task={task}
              queryKey={["tasks", "inbox"]}
            />
          ))
        )}

        {/* Add task form */}
        {showForm && (
          <div className="p-4">
            <TaskForm
              queryKey={["tasks", "inbox"]}
              onSuccess={() => setShowForm(false)}
              onCancel={() => setShowForm(false)}
            />
          </div>
        )}
      </div>

      {/* Add task button */}
      {!showForm && (
        <Button
          variant="ghost"
          className="mt-3 text-gray-400 hover:text-gray-600 gap-2"
          onClick={() => setShowForm(true)}
        >
          <Plus className="w-4 h-4" />
          Taak toevoegen
        </Button>
      )}
    </div>
  );
}
