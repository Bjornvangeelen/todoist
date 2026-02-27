"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { TaskItem } from "@/components/tasks/task-item";
import { TaskForm } from "@/components/tasks/task-form";
import { Button } from "@/components/ui/button";
import { Plus, Calendar, Sun } from "lucide-react";
import type { TaskWithRelations } from "@/types";

export default function TodayPage() {
  const [showForm, setShowForm] = useState(false);

  const today = new Date().toLocaleDateString("nl-NL", {
    weekday: "long",
    day: "numeric",
    month: "long",
  });

  const { data: tasks = [], isLoading } = useQuery<TaskWithRelations[]>({
    queryKey: ["tasks", "today"],
    queryFn: () => fetch("/api/tasks?filter=today").then((r) => r.json()),
  });

  // Separate overdue and today's tasks
  const todayDate = new Date();
  todayDate.setHours(0, 0, 0, 0);

  const overdue = tasks.filter(
    (t) => t.dueDate && new Date(t.dueDate) < todayDate
  );
  const todayTasks = tasks.filter(
    (t) => !t.dueDate || new Date(t.dueDate) >= todayDate
  );

  return (
    <div className="flex-1 p-6 max-w-3xl mx-auto w-full">
      <div className="flex items-center gap-3 mb-1">
        <Sun className="w-6 h-6 text-yellow-500" />
        <h1 className="text-2xl font-bold text-gray-900">Vandaag</h1>
      </div>
      <p className="text-sm text-gray-400 mb-6 ml-9 capitalize">{today}</p>

      {/* Overdue tasks */}
      {overdue.length > 0 && (
        <div className="mb-6">
          <h2 className="text-sm font-semibold text-red-500 mb-2 flex items-center gap-2">
            <Calendar className="w-4 h-4" />
            Achterstallig
          </h2>
          <div className="bg-white rounded-xl border border-red-100 overflow-hidden">
            {overdue.map((task) => (
              <TaskItem
                key={task.id}
                task={task}
                queryKey={["tasks", "today"]}
              />
            ))}
          </div>
        </div>
      )}

      {/* Today tasks */}
      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
        {isLoading ? (
          <div className="p-8 text-center text-gray-400 text-sm">Laden...</div>
        ) : todayTasks.length === 0 && !showForm ? (
          <div className="p-12 text-center">
            <Sun className="w-12 h-12 text-yellow-100 mx-auto mb-3" />
            <p className="text-gray-400 text-sm">Geen taken voor vandaag!</p>
            <p className="text-gray-300 text-xs mt-1">Geniet van je vrije dag 🎉</p>
          </div>
        ) : (
          todayTasks.map((task) => (
            <TaskItem
              key={task.id}
              task={task}
              queryKey={["tasks", "today"]}
            />
          ))
        )}

        {showForm && (
          <div className="p-4">
            <TaskForm
              queryKey={["tasks", "today"]}
              onSuccess={() => setShowForm(false)}
              onCancel={() => setShowForm(false)}
            />
          </div>
        )}
      </div>

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
