"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { TaskItem } from "@/components/tasks/task-item";
import { TaskForm } from "@/components/tasks/task-form";
import { Button } from "@/components/ui/button";
import { Plus, MoreHorizontal } from "lucide-react";
import { use } from "react";

interface ProjectPageProps {
  params: Promise<{ projectId: string }>;
}

export default function ProjectPage({ params }: ProjectPageProps) {
  const { projectId } = use(params);
  const [showForm, setShowForm] = useState(false);

  const { data: project, isLoading } = useQuery({
    queryKey: ["project", projectId],
    queryFn: () => fetch(`/api/projects/${projectId}`).then((r) => r.json()),
  });

  if (isLoading) {
    return (
      <div className="flex-1 p-6">
        <div className="text-gray-400 text-sm">Laden...</div>
      </div>
    );
  }

  if (!project || project.error) {
    return (
      <div className="flex-1 p-6">
        <div className="text-gray-400 text-sm">Project niet gevonden</div>
      </div>
    );
  }

  return (
    <div className="flex-1 p-6 max-w-3xl mx-auto w-full">
      <div className="flex items-center gap-3 mb-6">
        <span
          className="w-4 h-4 rounded-full"
          style={{ backgroundColor: project.color }}
        />
        <h1 className="text-2xl font-bold text-gray-900">{project.name}</h1>
        <button className="ml-auto text-gray-400 hover:text-gray-600">
          <MoreHorizontal className="w-5 h-5" />
        </button>
      </div>

      {/* Tasks without section */}
      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden mb-4">
        {project.tasks?.length === 0 && !showForm ? (
          <div className="p-8 text-center">
            <p className="text-gray-400 text-sm">Geen taken in dit project</p>
          </div>
        ) : (
          project.tasks?.map((task: { id: string; [key: string]: unknown }) => (
            <TaskItem
              key={task.id}
              task={task as Parameters<typeof TaskItem>[0]["task"]}
              queryKey={["project", projectId]}
            />
          ))
        )}

        {showForm && (
          <div className="p-4">
            <TaskForm
              projectId={projectId}
              queryKey={["project", projectId]}
              onSuccess={() => setShowForm(false)}
              onCancel={() => setShowForm(false)}
            />
          </div>
        )}
      </div>

      {/* Sections */}
      {project.sections?.map((section: { id: string; name: string; tasks: { id: string; [key: string]: unknown }[] }) => (
        <div key={section.id} className="mb-4">
          <h2 className="text-sm font-semibold text-gray-500 mb-2">{section.name}</h2>
          <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
            {section.tasks.map((task) => (
              <TaskItem
                key={task.id}
                task={task as Parameters<typeof TaskItem>[0]["task"]}
                queryKey={["project", projectId]}
              />
            ))}
          </div>
        </div>
      ))}

      {!showForm && (
        <Button
          variant="ghost"
          className="text-gray-400 hover:text-gray-600 gap-2"
          onClick={() => setShowForm(true)}
        >
          <Plus className="w-4 h-4" />
          Taak toevoegen
        </Button>
      )}
    </div>
  );
}
