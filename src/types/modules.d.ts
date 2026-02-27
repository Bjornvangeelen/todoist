// Type declarations for modules without proper TypeScript support

declare module "mailparser" {
  export function simpleParser(
    source: Buffer | string | NodeJS.ReadableStream,
    options?: Record<string, unknown>
  ): Promise<{
    subject?: string;
    from?: { text: string };
    to?: { text: string };
    date?: Date;
    text?: string;
    html?: string;
    attachments?: { filename?: string; content: Buffer; contentType: string }[];
  }>;
}

declare module "imapflow" {
  export class ImapFlow {
    constructor(options: {
      host: string;
      port: number;
      secure: boolean;
      auth: { user: string; pass: string };
      logger?: boolean | object;
    });
    connect(): Promise<void>;
    logout(): Promise<void>;
    mailboxOpen(mailbox: string): Promise<{ exists: number }>;
    fetch(
      range: string,
      options: {
        uid?: boolean;
        envelope?: boolean;
        bodyStructure?: boolean;
        source?: boolean;
      }
    ): AsyncIterable<{
      uid: number;
      envelope: {
        subject?: string;
        from?: { address?: string; name?: string }[];
        date: Date;
      };
    }>;
    download(
      range: string,
      part?: string,
      options?: { uid?: boolean }
    ): Promise<{ content: NodeJS.ReadableStream }>;
  }
}
