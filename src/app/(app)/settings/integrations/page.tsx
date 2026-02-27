"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Settings, RefreshCw, Check, AlertCircle, Loader2, Mail, Calendar } from "lucide-react";
import { toast } from "sonner";

export default function IntegrationsPage() {
  const [imapForm, setImapForm] = useState({
    host: "",
    port: 993,
    secure: true,
    user: "",
    password: "",
  });
  const [showImapForm, setShowImapForm] = useState(false);

  const syncMutation = useMutation({
    mutationFn: async (provider: "google" | "microsoft") => {
      const res = await fetch(`/api/integrations/${provider}/calendar/sync`, {
        method: "POST",
      });
      if (!res.ok) throw new Error(await res.text());
      return res.json();
    },
    onSuccess: (data, provider) => {
      toast.success(
        `${provider === "google" ? "Google" : "Microsoft"} Calendar gesynchroniseerd: ${data.synced} evenementen`
      );
    },
    onError: (error) => {
      toast.error(`Sync mislukt: ${error.message}`);
    },
  });

  const imapMutation = useMutation({
    mutationFn: async () => {
      const res = await fetch("/api/integrations/imap/connect", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...imapForm, port: Number(imapForm.port) }),
      });
      if (!res.ok) {
        const err = await res.json();
        throw new Error(err.error ?? "Verbinding mislukt");
      }
      return res.json();
    },
    onSuccess: () => {
      toast.success("IMAP verbinding opgeslagen!");
      setShowImapForm(false);
      setImapForm({ host: "", port: 993, secure: true, user: "", password: "" });
    },
    onError: (error) => {
      toast.error(error.message);
    },
  });

  return (
    <div className="flex-1 p-6 max-w-3xl mx-auto w-full">
      <div className="flex items-center gap-3 mb-6">
        <Settings className="w-6 h-6 text-gray-700" />
        <h1 className="text-2xl font-bold text-gray-900">Integraties</h1>
      </div>

      <div className="space-y-4">
        {/* Google */}
        <div className="bg-white border border-gray-200 rounded-xl p-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <svg viewBox="0 0 24 24" className="w-8 h-8">
                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
              </svg>
              <div>
                <h3 className="font-semibold text-gray-900">Google</h3>
                <p className="text-sm text-gray-500">Calendar + Gmail</p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => syncMutation.mutate("google")}
                disabled={syncMutation.isPending}
                className="gap-1.5"
              >
                {syncMutation.isPending ? (
                  <Loader2 className="w-3.5 h-3.5 animate-spin" />
                ) : (
                  <RefreshCw className="w-3.5 h-3.5" />
                )}
                Sync Calendar
              </Button>
            </div>
          </div>
          <p className="text-xs text-gray-400 mt-3">
            Google Calendar en Gmail worden gesynchroniseerd via je Google inlog.
            Log in met Google om deze integratie te activeren.
          </p>
        </div>

        {/* Microsoft */}
        <div className="bg-white border border-gray-200 rounded-xl p-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <svg viewBox="0 0 24 24" className="w-8 h-8">
                <path fill="#f25022" d="M1 1h10v10H1z"/>
                <path fill="#7fba00" d="M13 1h10v10H13z"/>
                <path fill="#00a4ef" d="M1 13h10v10H1z"/>
                <path fill="#ffb900" d="M13 13h10v10H13z"/>
              </svg>
              <div>
                <h3 className="font-semibold text-gray-900">Microsoft</h3>
                <p className="text-sm text-gray-500">Outlook + Calendar</p>
              </div>
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={() => syncMutation.mutate("microsoft")}
              disabled={syncMutation.isPending}
              className="gap-1.5"
            >
              {syncMutation.isPending ? (
                <Loader2 className="w-3.5 h-3.5 animate-spin" />
              ) : (
                <RefreshCw className="w-3.5 h-3.5" />
              )}
              Sync Calendar
            </Button>
          </div>
          <p className="text-xs text-gray-400 mt-3">
            Log in met Microsoft om Outlook en Calendar te synchroniseren.
          </p>
        </div>

        {/* IMAP */}
        <div className="bg-white border border-gray-200 rounded-xl p-6">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 bg-gray-100 rounded-lg flex items-center justify-center">
                <Mail className="w-4 h-4 text-gray-600" />
              </div>
              <div>
                <h3 className="font-semibold text-gray-900">IMAP Email</h3>
                <p className="text-sm text-gray-500">Universele email (Fastmail, etc.)</p>
              </div>
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setShowImapForm(!showImapForm)}
            >
              {showImapForm ? "Annuleren" : "Verbinden"}
            </Button>
          </div>

          {showImapForm && (
            <form
              onSubmit={(e) => {
                e.preventDefault();
                imapMutation.mutate();
              }}
              className="space-y-3 pt-4 border-t border-gray-100"
            >
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-xs font-medium text-gray-600 mb-1 block">IMAP Host</label>
                  <Input
                    placeholder="imap.fastmail.com"
                    value={imapForm.host}
                    onChange={(e) => setImapForm((f) => ({ ...f, host: e.target.value }))}
                    required
                  />
                </div>
                <div>
                  <label className="text-xs font-medium text-gray-600 mb-1 block">Poort</label>
                  <Input
                    type="number"
                    placeholder="993"
                    value={imapForm.port}
                    onChange={(e) => setImapForm((f) => ({ ...f, port: Number(e.target.value) }))}
                    required
                  />
                </div>
              </div>
              <div>
                <label className="text-xs font-medium text-gray-600 mb-1 block">E-mailadres</label>
                <Input
                  type="email"
                  placeholder="naam@voorbeeld.nl"
                  value={imapForm.user}
                  onChange={(e) => setImapForm((f) => ({ ...f, user: e.target.value }))}
                  required
                />
              </div>
              <div>
                <label className="text-xs font-medium text-gray-600 mb-1 block">Wachtwoord / App-wachtwoord</label>
                <Input
                  type="password"
                  value={imapForm.password}
                  onChange={(e) => setImapForm((f) => ({ ...f, password: e.target.value }))}
                  required
                />
              </div>
              <label className="flex items-center gap-2 text-sm text-gray-600 cursor-pointer">
                <input
                  type="checkbox"
                  checked={imapForm.secure}
                  onChange={(e) => setImapForm((f) => ({ ...f, secure: e.target.checked }))}
                  className="rounded"
                />
                SSL/TLS gebruiken
              </label>
              <Button
                type="submit"
                disabled={imapMutation.isPending}
                className="w-full"
              >
                {imapMutation.isPending ? (
                  <><Loader2 className="w-4 h-4 animate-spin" /> Verbinden...</>
                ) : (
                  "Verbinding testen en opslaan"
                )}
              </Button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}
