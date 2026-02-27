"use client";

import { useState } from "react";
import { Checkbox } from "@/components/ui/checkbox";
import { Badge } from "@/components/ui/badge";
import { cn, formatDate, isOverdue, PRIORITY_COLORS } from "@/lib/utils";
import { Calendar, Flag, MessageSquare, ChevronRight } from "lucide-react";
import type { TaskWithRelations } from "@/types";
import { useMutation, useQueryClient } from "@tanstack/react-query";

interface TaskItemProps {
  task: TaskWithRelations;
  onEdit?: (task: TaskWithRelations) => void;
  queryKey?: string[];
}

export function TaskItem({ task, onEdit, queryKey = ["tasks"] }: TaskItemProps) {
  const [isHovered, setIsHovered] = useState(false);
  const queryClient = useQueryClient();

  const completeMutation = useMutation({
    mutationFn: async () => {
      const res = await fetch(`/api/tasks/${task.id}/complete`, {
        method: "POST",
      });
      if (!res.ok) throw new Error("Fout bij voltooien van taak");
      return res.json();
    },
    onMutate: async () => {
      await queryClient.cancelQueries({ queryKey });
      const previous = queryClient.getQueryData(queryKey);
      queryClient.setQueryData(queryKey, (old: TaskWithRelations[] = []) =>
        old.map((t) =>
          t.id === task.id ? { ...t, isCompleted: !t.isCompleted } : t
        )
      );
      return { previous };
    },
    onError: (_err, _vars, context) => {
      queryClient.setQueryData(queryKey, context?.previous);
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey });
    },
  });

  const priorityColor = PRIORITY_COLORS[task.priority as 1 | 2 | 3 | 4];
  const dateStr = formatDate(task.dueDate);
  const overdue = isOverdue(task.dueDate) && !task.isCompleted;

  return (
    <div
      className={cn(
        "group flex items-start gap-3 px-4 py-3 border-b border-gray-100 transition-colors",
        isHovered ? "bg-gray-50" : "bg-white",
        task.isCompleted && "opacity-60"
      )}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      {/* Checkbox */}
      <div className="flex-shrink-0 mt-0.5">
        <Checkbox
          checked={task.isCompleted}
          onCheckedChange={() => completeMutation.mutate()}
          className={cn(
            "transition-colors",
            task.priority === 1 && "border-red-400 data-[state=checked]:bg-red-400",
            task.priority === 2 && "border-orange-400 data-[state=checked]:bg-orange-400",
            task.priority === 3 && "border-blue-400 data-[state=checked]:bg-blue-400"
          )}
        />
      </div>

      {/* Content */}
      <div
        className="flex-1 min-w-0 cursor-pointer"
        onClick={() => onEdit?.(task)}
      >
        <p
          className={cn(
            "text-sm text-gray-900",
            task.isCompleted && "line-through text-gray-400"
          )}
        >
          {task.title}
        </p>

        {/* Meta */}
        <div className="flex items-center gap-2 mt-1 flex-wrap">
          {/* Due date */}
          {dateStr && (
            <span
              className={cn(
                "flex items-center gap-1 text-xs",
                overdue ? "text-red-500" : "text-gray-400"
              )}
            >
              <Calendar className="w-3 h-3" />
              {dateStr}
            </span>
          )}

          {/* Priority */}
          {task.priority < 4 && (
            <span className={cn("flex items-center gap-1 text-xs", priorityColor)}>
              <Flag className="w-3 h-3" />
            </span>
          )}

          {/* Labels */}
          {task.labels?.map(({ label }) => (
            <Badge
              key={label.id}
              variant="secondary"
              className="text-xs px-1.5 py-0"
              style={{ backgroundColor: label.color + "20", color: label.color }}
            >
              {label.name}
            </Badge>
          ))}

          {/* Project */}
          {task.project && (
            <span className="flex items-center gap-1 text-xs text-gray-400">
              <span
                className="w-2 h-2 rounded-full"
                style={{ backgroundColor: task.project.color }}
              />
              {task.project.name}
            </span>
          )}

          {/* Comments count */}
          {(task.comments?.length ?? 0) > 0 && (
            <span className="flex items-center gap-1 text-xs text-gray-400">
              <MessageSquare className="w-3 h-3" />
              {task.comments!.length}
            </span>
          )}
        </div>
      </div>

      {/* Actions */}
      {isHovered && (
        <button
          onClick={() => onEdit?.(task)}
          className="flex-shrink-0 text-gray-400 hover:text-gray-600 transition-colors"
        >
          <ChevronRight className="w-4 h-4" />
        </button>
      )}
    </div>
  );
}
