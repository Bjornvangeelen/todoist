"use client";

import { useQuery } from "@tanstack/react-query";
import { TaskItem } from "@/components/tasks/task-item";
import { CalendarDays } from "lucide-react";
import type { TaskWithRelations } from "@/types";
import { isSameDay } from "@/lib/utils";

export default function UpcomingPage() {
  const { data: tasks = [], isLoading } = useQuery<TaskWithRelations[]>({
    queryKey: ["tasks", "upcoming"],
    queryFn: () => fetch("/api/tasks?filter=upcoming").then((r) => r.json()),
  });

  // Group tasks by date
  const today = new Date();
  const days = Array.from({ length: 7 }, (_, i) => {
    const d = new Date(today);
    d.setDate(d.getDate() + i);
    d.setHours(0, 0, 0, 0);
    return d;
  });

  const tasksByDay = days.map((day) => ({
    day,
    tasks: tasks.filter(
      (t) => t.dueDate && isSameDay(new Date(t.dueDate), day)
    ),
  }));

  const dayNames = ["Zondag", "Maandag", "Dinsdag", "Woensdag", "Donderdag", "Vrijdag", "Zaterdag"];
  const monthNames = ["jan", "feb", "mrt", "apr", "mei", "jun", "jul", "aug", "sep", "okt", "nov", "dec"];

  return (
    <div className="flex-1 p-6 max-w-3xl mx-auto w-full">
      <div className="flex items-center gap-3 mb-6">
        <CalendarDays className="w-6 h-6 text-gray-700" />
        <h1 className="text-2xl font-bold text-gray-900">Aankomend</h1>
      </div>

      {isLoading ? (
        <div className="text-center text-gray-400 text-sm py-8">Laden...</div>
      ) : (
        <div className="space-y-6">
          {tasksByDay.map(({ day, tasks: dayTasks }) => {
            const isToday = isSameDay(day, today);
            const isTomorrow = isSameDay(
              day,
              new Date(today.getFullYear(), today.getMonth(), today.getDate() + 1)
            );

            let label = `${dayNames[day.getDay()]} ${day.getDate()} ${monthNames[day.getMonth()]}`;
            if (isToday) label = `Vandaag · ${day.getDate()} ${monthNames[day.getMonth()]}`;
            if (isTomorrow) label = `Morgen · ${day.getDate()} ${monthNames[day.getMonth()]}`;

            return (
              <div key={day.toISOString()}>
                <h2
                  className={`text-sm font-semibold mb-2 capitalize ${
                    isToday ? "text-red-500" : "text-gray-600"
                  }`}
                >
                  {label}
                </h2>
                {dayTasks.length === 0 ? (
                  <div className="text-xs text-gray-300 pl-2">Geen taken</div>
                ) : (
                  <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
                    {dayTasks.map((task) => (
                      <TaskItem
                        key={task.id}
                        task={task}
                        queryKey={["tasks", "upcoming"]}
                      />
                    ))}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
