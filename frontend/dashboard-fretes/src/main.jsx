import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { createRoot } from "react-dom/client";
import {
  AlertTriangle,
  ArrowRight,
  Box,
  CheckCircle2,
  CircleSlash,
  Clock3,
  PackageCheck,
  Radio,
  RefreshCw,
  Search,
  Send,
  Truck,
  Wifi,
  Waypoints,
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { currency, kg } from "@/lib/utils";
import "./styles.css";

const STATUS = {
  EMITIDO: { label: "Emitido", css: "var(--status-emitido)", icon: Clock3 },
  SAIDA_CONFIRMADA: { label: "Saída confirmada", css: "var(--status-saida-confirmada)", icon: Send },
  EM_TRANSITO: { label: "Em trânsito", css: "var(--status-em-transito)", icon: Truck },
  ENTREGUE: { label: "Entregue", css: "var(--status-entregue)", icon: CheckCircle2 },
  NAO_ENTREGUE: { label: "Não entregue", css: "var(--status-nao-entregue)", icon: AlertTriangle },
  CANCELADO: { label: "Cancelado", css: "var(--status-cancelado)", icon: CircleSlash },
};

const STATUS_ORDER = ["EMITIDO", "SAIDA_CONFIRMADA", "EM_TRANSITO", "ENTREGUE"];
const STOMP_FRAME_END = "\0";
const STOMP_RECONNECT_DELAY_MS = 5000;
const STOMP_HEARTBEAT_MS = 10000;
const EVENT_HYDRATION_DELAY_MS = 1500;

function getContextPath() {
  const path = window.location.pathname || "";
  const marker = "/jsp/";
  const index = path.indexOf(marker);
  if (index >= 0) {
    return path.slice(0, index);
  }
  return "";
}

const CONTEXT_PATH = getContextPath();
const ROOT_ELEMENT = document.getElementById("dashboard-fretes-root");

function endpoint(path) {
  return `${CONTEXT_PATH}${path}`;
}

function trimToNull(value) {
  if (value == null) return null;
  const normalized = String(value).trim();
  return normalized ? normalized : null;
}

function defaultMensageriaWsUrl() {
  const protocol = window.location.protocol === "https:" ? "wss" : "ws";
  const host = window.location.hostname || "localhost";
  return `${protocol}://${host}:8082/ws-fretes`;
}

function readDashboardConfig(root) {
  return {
    websocketUrl: trimToNull(root && root.dataset ? root.dataset.mensageriaWsUrl : "") || defaultMensageriaWsUrl(),
    websocketTopic: trimToNull(root && root.dataset ? root.dataset.mensageriaTopic : "") || "/topic/fretes",
  };
}

const DASHBOARD_CONFIG = readDashboardConfig(ROOT_ELEMENT);

function formatDate(value) {
  if (!value) return "Não informado";
  const normalized = value.includes("T") ? value : `${value}T00:00:00`;
  const date = new Date(normalized);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat("pt-BR").format(date);
}

function formatDateTime(value) {
  if (!value) return "Não informado";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat("pt-BR", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(date);
}

function numberOrNull(value) {
  if (value == null || value === "") return null;
  const parsed = Number(value);
  return Number.isNaN(parsed) ? null : parsed;
}

function normalizeFreight(item) {
  return {
    id: item.id,
    numero: item.numero || "Sem número",
    origem: item.origem || "Origem não informada",
    destino: item.destino || "Destino não informado",
    status: STATUS[item.status] ? item.status : "EMITIDO",
    remetente: item.remetente || "Não informado",
    destinatario: item.destinatario || "Não informado",
    motorista: item.motorista || "Não informado",
    placa: item.veiculo || "Não informado",
    descricaoCarga: item.descricaoCarga || "Carga não informada",
    pesoKg: Number(item.pesoKg || 0),
    volumes: item.volumes == null ? null : Number(item.volumes),
    valorFrete: item.valorFrete == null ? null : Number(item.valorFrete),
    aliquotaIcms: item.aliquotaIcms == null ? null : Number(item.aliquotaIcms),
    valorIcms: item.valorIcms == null ? null : Number(item.valorIcms),
    valorTotal: Number(item.valorTotal || 0),
    dataEmissao: item.dataEmissao || "",
    dataPrevisaoEntrega: item.dataPrevisaoEntrega || "",
    dataSaida: item.dataSaida || "",
    dataEntrega: item.dataEntrega || "",
  };
}

function inferStatusFromEvent(eventName) {
  const eventMap = {
    FRETE_CRIADO: "EMITIDO",
    FRETE_SAIDA_CONFIRMADA: "SAIDA_CONFIRMADA",
    FRETE_EM_TRANSITO: "EM_TRANSITO",
    FRETE_ENTREGUE: "ENTREGUE",
    FRETE_NAO_ENTREGUE: "NAO_ENTREGUE",
    FRETE_CANCELADO: "CANCELADO",
  };
  return eventMap[eventName] || null;
}

function extractFreightPatch(message) {
  if (!message || typeof message !== "object") return null;

  const freight = message.frete && typeof message.frete === "object" ? message.frete : message;
  if (!freight || typeof freight !== "object") return null;

  const inferredStatus = STATUS[freight.status] ? freight.status : inferStatusFromEvent(message.evento);
  const patch = {};

  if (freight.id != null && freight.id !== "") patch.id = Number(freight.id);
  if (trimToNull(freight.numero)) patch.numero = trimToNull(freight.numero);
  if (trimToNull(freight.origem)) patch.origem = trimToNull(freight.origem);
  if (trimToNull(freight.destino)) patch.destino = trimToNull(freight.destino);
  if (inferredStatus) patch.status = inferredStatus;

  const pesoKg = numberOrNull(freight.pesoKg);
  const valorTotal = numberOrNull(freight.valorTotal);
  if (pesoKg != null) patch.pesoKg = pesoKg;
  if (valorTotal != null) patch.valorTotal = valorTotal;

  if (patch.id == null && !patch.numero) return null;
  return patch;
}

function describeRealtimeEvent(message, patch) {
  const status = patch && patch.status ? STATUS[patch.status] : null;
  const numero = patch && patch.numero ? patch.numero : "frete sem número";
  const tipo = trimToNull(message && message.evento);
  const when = trimToNull(message && message.dataEvento);

  if (status && when) {
    return `${numero} recebido via mensageria: ${status.label.toLowerCase()} em ${formatDateTime(when)}`;
  }
  if (status) {
    return `${numero} recebido via mensageria: ${status.label.toLowerCase()}`;
  }
  if (tipo) {
    return `${numero} recebido via mensageria: ${tipo}`;
  }
  return "Evento recebido da API de mensageria";
}

function buildStompFrame(command, headers, body) {
  const safeBody = body || "";
  const lines = [command];

  Object.keys(headers || {}).forEach((key) => {
    lines.push(`${key}:${headers[key]}`);
  });

  return `${lines.join("\n")}\n\n${safeBody}${STOMP_FRAME_END}`;
}

function parseStompFrames(payload) {
  const text = typeof payload === "string" ? payload : "";
  return text
    .split(STOMP_FRAME_END)
    .map((frame) => frame.replace(/^\n+/, "").trim())
    .filter(Boolean)
    .map((frame) => {
      const separator = frame.indexOf("\n\n");
      const head = separator >= 0 ? frame.slice(0, separator) : frame;
      const body = separator >= 0 ? frame.slice(separator + 2) : "";
      const lines = head.split("\n").map((line) => line.replace(/\r/g, ""));
      const command = lines.shift() || "";
      const headers = {};

      lines.forEach((line) => {
        const divider = line.indexOf(":");
        if (divider > 0) {
          headers[line.slice(0, divider)] = line.slice(divider + 1);
        }
      });

      return { command, headers, body };
    });
}

function stompHostHeader(url) {
  try {
    return new URL(url).host;
  } catch (error) {
    return window.location.host || "localhost";
  }
}

async function fetchJson(url, options) {
  const response = await fetch(url, options);
  const text = await response.text();
  let data = null;

  if (text) {
    try {
      data = JSON.parse(text);
    } catch (error) {
      data = null;
    }
  }

  if (!response.ok) {
    const message = data && data.mensagem ? data.mensagem : "Falha na comunicação com o legado.";
    throw new Error(message);
  }

  return data;
}

function nextAction(status) {
  if (status === "EMITIDO") return "confirmarSaida";
  if (status === "SAIDA_CONFIRMADA") return "iniciarTransito";
  if (status === "EM_TRANSITO") return "entregar";
  return null;
}

function actionLabel(status) {
  if (status === "EMITIDO") return "Confirmar saída";
  if (status === "SAIDA_CONFIRMADA") return "Iniciar trânsito";
  if (status === "EM_TRANSITO") return "Marcar entregue";
  return "Finalizado";
}

function emptyDeliveryDraft() {
  return {
    freight: null,
    nomeRecebedor: "",
    documentoRecebedor: "",
    error: "",
  };
}

function TruckSvg({ status }) {
  const color = STATUS[status].css;
  return (
    <svg viewBox="0 0 210 92" className="h-16 w-36 truck-glow" style={{ "--truck-color": color }}>
      <path d="M20 59h105V25H20z" fill={color} opacity="0.96" />
      <path d="M125 41h28l23 18v16h-51z" fill={color} />
      <path d="M139 45h12l13 11h-25z" fill="oklch(0.18 0.005 280)" opacity="0.82" />
      <path d="M12 75h180" stroke={color} strokeWidth="5" strokeLinecap="round" />
      <circle cx="55" cy="76" r="10" fill="oklch(0.12 0.005 280)" stroke={color} strokeWidth="4" />
      <circle cx="151" cy="76" r="10" fill="oklch(0.12 0.005 280)" stroke={color} strokeWidth="4" />
      <path d="M33 38h74M33 49h74" stroke="oklch(0.18 0.005 280)" strokeWidth="4" opacity="0.35" />
    </svg>
  );
}

function StatusBadge({ status }) {
  return (
    <Badge style={{ backgroundColor: STATUS[status].css, color: "oklch(0.12 0.005 280)" }}>
      {STATUS[status].label}
    </Badge>
  );
}

function MetricCard({ status, count }) {
  const meta = STATUS[status];
  const Icon = meta.icon;
  return (
    <Card className="min-h-[72px]">
      <CardContent className="flex items-center justify-between p-4">
        <div className="flex items-center gap-3">
          <span className="flex h-8 w-8 items-center justify-center rounded-md" style={{ backgroundColor: meta.css }}>
            <Icon className="h-4 w-4 text-background" />
          </span>
          <div>
            <div className="text-[10px] font-bold uppercase text-muted-foreground">{meta.label}</div>
            <div className="text-xs text-muted-foreground">Fretes</div>
          </div>
        </div>
        <strong className="text-xl">{count}</strong>
      </CardContent>
    </Card>
  );
}

function StatusTrail({ status }) {
  const activeIndex = STATUS_ORDER.indexOf(status);
  const color = STATUS[status].css;
  return (
    <div className="rounded-md border border-border bg-background/60 p-3" style={{ "--truck-color": color }}>
      <div className="relative h-[78px]">
        <div className="absolute left-0 right-0 top-[48px] h-1 rounded-full bg-border" />
        <div className="absolute left-0 top-[48px] h-1 rounded-full status-rail" style={{ width: `${Math.max(activeIndex, 0) * 33}%` }} />
        <div className="absolute left-0 right-0 top-[20px] flex items-end justify-between">
          <span className="text-[9px] text-muted-foreground">Origem</span>
          <TruckSvg status={status} />
          <span className="text-[9px] text-muted-foreground">Destino</span>
        </div>
        <div className="absolute left-0 right-0 top-[45px] flex justify-between">
          {STATUS_ORDER.map((item, index) => (
            <span
              key={item}
              className={index <= activeIndex ? "pulse-dot h-2 w-2 rounded-full" : "h-2 w-2 rounded-full bg-border"}
              style={index <= activeIndex ? { backgroundColor: STATUS[item].css } : undefined}
            />
          ))}
        </div>
      </div>
    </div>
  );
}

function OperationalBlock({ freight }) {
  return (
    <div className="grid gap-3 text-xs md:grid-cols-2">
      <div className="rounded-md border border-border bg-background/50 p-3">
        <div className="mb-2 flex items-center gap-2 text-[10px] uppercase text-muted-foreground">
          <Box className="h-3 w-3" /> Carga operacional
        </div>
        <div className="grid gap-2">
          <div>
            <div className="text-muted-foreground">Descrição</div>
            <strong>{freight.descricaoCarga}</strong>
          </div>
          <div className="flex justify-between gap-3">
            <span className="text-muted-foreground">Peso bruto</span>
            <span>{kg(freight.pesoKg)}</span>
          </div>
          <div className="flex justify-between gap-3">
            <span className="text-muted-foreground">Volumes</span>
            <span>{freight.volumes == null ? "Não informado" : freight.volumes}</span>
          </div>
        </div>
      </div>
      <div className="rounded-md border border-border bg-background/50 p-3">
        <div className="mb-2 flex items-center gap-2 text-[10px] uppercase text-muted-foreground">
          <PackageCheck className="h-3 w-3" /> Financeiro real
        </div>
        <div className="grid gap-2">
          <div className="flex justify-between gap-3">
            <span className="text-muted-foreground">Valor frete</span>
            <span>{freight.valorFrete == null ? "Não informado" : currency(freight.valorFrete)}</span>
          </div>
          <div className="flex justify-between gap-3">
            <span className="text-muted-foreground">ICMS</span>
            <span>
              {freight.aliquotaIcms == null || freight.valorIcms == null
                ? "Não informado"
                : `${freight.aliquotaIcms}% · ${currency(freight.valorIcms)}`}
            </span>
          </div>
          <div className="flex justify-between gap-3 border-t border-border pt-2 font-semibold">
            <span>Valor total</span>
            <span>{currency(freight.valorTotal)}</span>
          </div>
        </div>
      </div>
    </div>
  );
}

function FreightCard({ freight, onAdvance, updatingId }) {
  const color = STATUS[freight.status].css;
  const action = nextAction(freight.status);
  const disabled = !action || updatingId === freight.id;
  const detailUrl = endpoint(`/FreteController?acao=detalhar&id=${freight.id}`);

  return (
    <Card className="overflow-hidden">
      <CardHeader className="flex flex-row items-start justify-between gap-3">
        <div>
          <div className="text-[10px] text-muted-foreground">{freight.numero}</div>
          <CardTitle className="text-base">{freight.origem} <ArrowRight className="inline h-3 w-3" /> {freight.destino}</CardTitle>
        </div>
        <StatusBadge status={freight.status} />
      </CardHeader>
      <CardContent className="grid gap-4">
        <StatusTrail status={freight.status} />
        <div className="grid grid-cols-2 gap-3 text-xs md:grid-cols-4">
          <div><div className="text-muted-foreground">Remetente</div><strong>{freight.remetente}</strong></div>
          <div><div className="text-muted-foreground">Destinatário</div><strong>{freight.destinatario}</strong></div>
          <div><div className="text-muted-foreground">Motorista</div><strong>{freight.motorista}</strong></div>
          <div><div className="text-muted-foreground">Placa</div><strong>{freight.placa}</strong></div>
        </div>
        <OperationalBlock freight={freight} />
        <div className="grid gap-2 text-[11px] text-muted-foreground md:grid-cols-3">
          <span>Emissão {formatDateTime(freight.dataEmissao)}</span>
          <span>Previsão {formatDate(freight.dataPrevisaoEntrega)}</span>
          <span>{freight.dataEntrega ? `Entrega ${formatDateTime(freight.dataEntrega)}` : `Saída ${formatDateTime(freight.dataSaida)}`}</span>
        </div>
        <div className="flex items-center justify-between gap-3 text-[11px] text-muted-foreground">
          <span>Sincronizado com o legado via `FreteController`.</span>
          <div className="flex items-center gap-2">
            <Button
              size="sm"
              variant="outline"
              onClick={() => { window.location.href = detailUrl; }}
            >
              <Waypoints className="h-4 w-4" />
              Ocorrências
            </Button>
            <Button
              size="sm"
              variant="outline"
              style={{ borderColor: color }}
              disabled={disabled}
              onClick={() => onAdvance(freight)}
            >
              {updatingId === freight.id ? <RefreshCw className="h-4 w-4 animate-spin" /> : null}
              {actionLabel(freight.status)}
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

function App() {
  const [freights, setFreights] = useState([]);
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [publishing, setPublishing] = useState(false);
  const [updatingId, setUpdatingId] = useState(null);
  const [deliveryDraft, setDeliveryDraft] = useState(emptyDeliveryDraft);
  const [lastEvent, setLastEvent] = useState("Aguardando eventos da API de mensageria");
  const [lastSync, setLastSync] = useState("Aguardando carga inicial do legado");
  const [socketConnected, setSocketConnected] = useState(false);
  const reconnectTimerRef = useRef(null);
  const heartbeatTimerRef = useRef(null);
  const hydrateTimerRef = useRef(null);
  const websocketRef = useRef(null);

  const counters = useMemo(() => {
    return Object.keys(STATUS).reduce((acc, key) => {
      acc[key] = freights.filter((item) => item.status === key).length;
      return acc;
    }, {});
  }, [freights]);

  const filtered = useMemo(() => {
    const value = query.trim().toLowerCase();
    if (!value) return freights;
    return freights.filter((freight) =>
      [
        freight.numero,
        freight.origem,
        freight.destino,
        freight.motorista,
        freight.placa,
        freight.remetente,
        freight.destinatario,
        STATUS[freight.status].label,
      ]
        .join(" ")
        .toLowerCase()
        .includes(value)
    );
  }, [freights, query]);

  const loadFreights = useCallback(async (silent) => {
    if (silent) {
      setRefreshing(true);
    } else {
      setLoading(true);
    }

    try {
      const data = await fetchJson(endpoint(`/FreteController?acao=buscar&filtro=`));
      const normalized = Array.isArray(data) ? data.map(normalizeFreight) : [];
      setFreights(normalized);
      setLastSync(`Legado sincronizado: ${normalized.length} registro(s) carregados`);
    } catch (error) {
      setLastSync(error.message);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  const scheduleHydration = useCallback(() => {
    if (hydrateTimerRef.current) {
      window.clearTimeout(hydrateTimerRef.current);
    }
    hydrateTimerRef.current = window.setTimeout(() => {
      loadFreights(true);
    }, EVENT_HYDRATION_DELAY_MS);
  }, [loadFreights]);

  const applyRealtimeEvent = useCallback((message) => {
    const patch = extractFreightPatch(message);
    if (!patch) return;

    setFreights((current) => {
      const index = current.findIndex((item) => {
        if (patch.id != null && item.id === patch.id) return true;
        return patch.numero && item.numero === patch.numero;
      });

      if (index < 0) {
        return [normalizeFreight(patch)].concat(current);
      }

      const merged = normalizeFreight(Object.assign({}, current[index], patch));
      const next = current.slice();
      next[index] = merged;
      return next;
    });

    setLastEvent(describeRealtimeEvent(message, patch));
    scheduleHydration();
  }, [scheduleHydration]);

  function closeDeliveryModal() {
    setDeliveryDraft(emptyDeliveryDraft());
  }

  async function advance(freight) {
    const action = nextAction(freight.status);
    if (!action) return;
    if (action === "entregar") {
      setDeliveryDraft({
        freight,
        nomeRecebedor: "",
        documentoRecebedor: "",
        error: "",
      });
      return;
    }

    setUpdatingId(freight.id);
    try {
      const body = new URLSearchParams();
      body.set("acao", action);
      body.set("id", String(freight.id));

      const result = await fetchJson(endpoint("/FreteController"), {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
        },
        body: body.toString(),
      });

      setLastEvent(result && result.mensagem ? result.mensagem : `Frete ${freight.numero} atualizado`);
      await loadFreights(true);
    } catch (error) {
      setLastEvent(error.message);
    } finally {
      setUpdatingId(null);
    }
  }

  async function submitDelivery(event) {
    event.preventDefault();
    if (!deliveryDraft.freight) return;

    const nomeRecebedor = deliveryDraft.nomeRecebedor.trim();
    const documentoRecebedor = deliveryDraft.documentoRecebedor.trim();

    if (!nomeRecebedor || !documentoRecebedor) {
      setDeliveryDraft((current) => ({
        ...current,
        error: "Informe o nome e o documento do recebedor para concluir a entrega.",
      }));
      return;
    }

    setUpdatingId(deliveryDraft.freight.id);
    try {
      const body = new URLSearchParams();
      body.set("acao", "entregar");
      body.set("id", String(deliveryDraft.freight.id));
      body.set("tipoOcorrencia", "ENTREGA_REALIZADA");
      body.set("nomeRecebedor", nomeRecebedor);
      body.set("documentoRecebedor", documentoRecebedor);

      const result = await fetchJson(endpoint("/FreteController"), {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
        },
        body: body.toString(),
      });

      const numeroFrete = deliveryDraft.freight.numero;
      closeDeliveryModal();
      setLastEvent(result && result.mensagem ? result.mensagem : `Frete ${numeroFrete} entregue`);
      await loadFreights(true);
    } catch (error) {
      setDeliveryDraft((current) => ({
        ...current,
        error: error.message,
      }));
      setLastEvent(error.message);
    } finally {
      setUpdatingId(null);
    }
  }

  async function publishPending() {
    if (publishing) return;
    setPublishing(true);

    try {
      const body = new URLSearchParams();
      body.set("acao", "enviarPendentes");

      const result = await fetchJson(endpoint("/MensageriaController"), {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
        },
        body: body.toString(),
      });

      setLastEvent(
        `Mensageria real: ${result.total || 0} evento(s), ${result.enviados || 0} enviado(s), ${result.erros || 0} erro(s)`
      );
    } catch (error) {
      setLastEvent(error.message);
    } finally {
      setPublishing(false);
    }
  }

  useEffect(() => {
    loadFreights(false);

    const timer = window.setInterval(() => {
      loadFreights(true);
    }, 15000);

    return () => window.clearInterval(timer);
  }, [loadFreights]);

  useEffect(() => {
    let cancelled = false;

    function clearHeartbeat() {
      if (heartbeatTimerRef.current) {
        window.clearInterval(heartbeatTimerRef.current);
        heartbeatTimerRef.current = null;
      }
    }

    function clearReconnect() {
      if (reconnectTimerRef.current) {
        window.clearTimeout(reconnectTimerRef.current);
        reconnectTimerRef.current = null;
      }
    }

    function scheduleReconnect() {
      clearReconnect();
      if (cancelled) return;
      reconnectTimerRef.current = window.setTimeout(() => {
        connect();
      }, STOMP_RECONNECT_DELAY_MS);
    }

    function disconnectSocket() {
      clearHeartbeat();
      if (websocketRef.current) {
        const socket = websocketRef.current;
        websocketRef.current = null;
        try {
          socket.onopen = null;
          socket.onmessage = null;
          socket.onerror = null;
          socket.onclose = null;
          socket.close();
        } catch (error) {
          // encerramento defensivo
        }
      }
    }

    function connect() {
      disconnectSocket();
      setSocketConnected(false);

      const socket = new window.WebSocket(DASHBOARD_CONFIG.websocketUrl);
      websocketRef.current = socket;

      socket.onopen = () => {
        socket.send(
          buildStompFrame("CONNECT", {
            "accept-version": "1.2,1.1",
            host: stompHostHeader(DASHBOARD_CONFIG.websocketUrl),
            "heart-beat": `${STOMP_HEARTBEAT_MS},${STOMP_HEARTBEAT_MS}`,
          })
        );
      };

      socket.onmessage = (event) => {
        parseStompFrames(event.data).forEach((frame) => {
          if (frame.command === "CONNECTED") {
            setSocketConnected(true);
            setLastEvent("Canal em tempo real conectado à API de mensageria");
            socket.send(
              buildStompFrame("SUBSCRIBE", {
                id: "fretes-dashboard",
                destination: DASHBOARD_CONFIG.websocketTopic,
                ack: "auto",
              })
            );

            clearHeartbeat();
            heartbeatTimerRef.current = window.setInterval(() => {
              if (socket.readyState === window.WebSocket.OPEN) {
                socket.send("\n");
              }
            }, STOMP_HEARTBEAT_MS);
            return;
          }

          if (frame.command === "MESSAGE") {
            try {
              const payload = JSON.parse(frame.body);
              applyRealtimeEvent(payload);
            } catch (error) {
              setLastEvent("Evento em tempo real recebido com payload inválido");
            }
            return;
          }

          if (frame.command === "ERROR") {
            const message = trimToNull(frame.headers.message) || trimToNull(frame.body) || "Falha na conexão STOMP";
            setLastEvent(message);
            setSocketConnected(false);
          }
        });
      };

      socket.onerror = () => {
        setSocketConnected(false);
      };

      socket.onclose = () => {
        clearHeartbeat();
        setSocketConnected(false);
        if (!cancelled) {
          setLastEvent("Conexão em tempo real perdida. Tentando reconectar...");
          scheduleReconnect();
        }
      };
    }

    connect();

    return () => {
      cancelled = true;
      clearReconnect();
      clearHeartbeat();
      if (hydrateTimerRef.current) {
        window.clearTimeout(hydrateTimerRef.current);
        hydrateTimerRef.current = null;
      }
      disconnectSocket();
    };
  }, [applyRealtimeEvent]);

  return (
    <main className="min-h-screen bg-background text-foreground">
      <header className="sticky top-0 z-20 border-b border-border bg-background/95 backdrop-blur">
        <div className="mx-auto flex max-w-[1480px] items-center justify-between px-4 py-3">
          <div className="flex items-center gap-3">
            <span className="flex h-9 w-9 items-center justify-center rounded-md bg-primary text-primary-foreground">
              <Truck className="h-5 w-5" />
            </span>
            <div>
              <h1 className="text-base font-bold">Dashboard de Fretes</h1>
              <p className="text-xs text-muted-foreground">Dados reais do legado + tempo real da mensageria</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <Button size="sm" variant="outline" onClick={() => loadFreights(true)} disabled={refreshing || loading}>
              {refreshing ? <RefreshCw className="h-4 w-4 animate-spin" /> : <RefreshCw className="h-4 w-4" />}
              Atualizar
            </Button>
            <Button size="sm" onClick={() => { window.location.href = endpoint("/FreteController"); }}>
              <PackageCheck className="h-4 w-4" /> Novo frete
            </Button>
          </div>
        </div>
      </header>

      <div className="mx-auto grid max-w-[1480px] gap-4 px-4 py-4">
        <section className="grid gap-3 md:grid-cols-3 xl:grid-cols-6">
          {Object.keys(STATUS).map((status) => (
            <MetricCard key={status} status={status} count={counters[status] || 0} />
          ))}
        </section>

        <section className="grid gap-4 lg:grid-cols-[1.5fr_0.7fr]">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Radio className="h-4 w-4 text-primary" /> Mensageria em tempo real
              </CardTitle>
            </CardHeader>
            <CardContent className="grid gap-3">
              <div className="rounded-md border border-border bg-background/50 p-3 text-sm text-muted-foreground">
                O legado envia os payloads reais salvos em `evento_sistema`, e a API devolve os eventos ao dashboard via STOMP/WebSocket.
              </div>
              <div className="flex items-center justify-between gap-3">
                <div className="grid gap-2 text-xs text-muted-foreground">
                  <div className="flex items-center gap-2">
                    <Wifi className={`h-4 w-4 ${socketConnected ? "text-primary" : "text-muted-foreground"}`} />
                    <span>{socketConnected ? "Canal STOMP ativo" : "Reconectando ao canal STOMP"}</span>
                  </div>
                  <div>{lastEvent}</div>
                  <div>{lastSync}</div>
                </div>
                <Button onClick={publishPending} disabled={publishing}>
                  {publishing ? <RefreshCw className="h-4 w-4 animate-spin" /> : <Send className="h-4 w-4" />}
                  Enviar pendentes
                </Button>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Search className="h-4 w-4 text-primary" /> Buscar frete
              </CardTitle>
            </CardHeader>
            <CardContent className="grid gap-2">
              <Input
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                placeholder="Número, placa, motorista, cliente..."
              />
              <p className="text-xs text-muted-foreground">
                {filtered.length} de {freights.length} fretes reais
              </p>
            </CardContent>
          </Card>
        </section>

        <section className="grid gap-4 lg:grid-cols-2">
          {!loading && filtered.length === 0 ? (
            <Card className="lg:col-span-2">
              <CardContent className="flex min-h-[180px] items-center justify-center text-sm text-muted-foreground">
                Nenhum frete encontrado com os filtros atuais.
              </CardContent>
            </Card>
          ) : null}

          {filtered.map((freight) => (
            <FreightCard key={freight.id} freight={freight} onAdvance={advance} updatingId={updatingId} />
          ))}
        </section>

        {loading ? (
          <Card>
            <CardContent className="flex min-h-[180px] items-center justify-center gap-3 text-sm text-muted-foreground">
              <RefreshCw className="h-4 w-4 animate-spin" />
              Carregando fretes reais do legado...
            </CardContent>
          </Card>
        ) : null}
      </div>

      {deliveryDraft.freight ? (
        <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/65 px-4 backdrop-blur-sm">
          <Card className="w-full max-w-lg border-primary/30 shadow-2xl">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <PackageCheck className="h-5 w-5 text-primary" />
                Confirmar entrega do frete {deliveryDraft.freight.numero}
              </CardTitle>
            </CardHeader>
            <CardContent>
              <form className="grid gap-4" onSubmit={submitDelivery}>
                <div className="grid gap-2">
                  <label
                    className="text-xs font-semibold uppercase tracking-[0.12em] text-muted-foreground"
                    htmlFor="deliveryNomeRecebedor"
                  >
                    Nome do recebedor
                  </label>
                  <Input
                    id="deliveryNomeRecebedor"
                    value={deliveryDraft.nomeRecebedor}
                    maxLength={200}
                    placeholder="Quem recebeu a carga?"
                    onChange={(event) =>
                      setDeliveryDraft((current) => ({
                        ...current,
                        nomeRecebedor: event.target.value,
                        error: "",
                      }))
                    }
                  />
                </div>
                <div className="grid gap-2">
                  <label
                    className="text-xs font-semibold uppercase tracking-[0.12em] text-muted-foreground"
                    htmlFor="deliveryDocumentoRecebedor"
                  >
                    Documento do recebedor
                  </label>
                  <Input
                    id="deliveryDocumentoRecebedor"
                    value={deliveryDraft.documentoRecebedor}
                    maxLength={30}
                    placeholder="CPF, RG ou outro documento"
                    onChange={(event) =>
                      setDeliveryDraft((current) => ({
                        ...current,
                        documentoRecebedor: event.target.value,
                        error: "",
                      }))
                    }
                  />
                </div>
                {deliveryDraft.error ? (
                  <div className="rounded-md border border-[color:var(--status-nao-entregue)]/40 bg-[color:var(--status-nao-entregue)]/10 px-3 py-2 text-sm text-[color:var(--status-nao-entregue)]">
                    {deliveryDraft.error}
                  </div>
                ) : null}
                <div className="flex justify-end gap-2">
                  <Button type="button" size="sm" variant="outline" onClick={closeDeliveryModal}>
                    Cancelar
                  </Button>
                  <Button type="submit" size="sm" disabled={updatingId === deliveryDraft.freight.id}>
                    {updatingId === deliveryDraft.freight.id ? <RefreshCw className="h-4 w-4 animate-spin" /> : <PackageCheck className="h-4 w-4" />}
                    Confirmar entrega
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>
        </div>
      ) : null}
    </main>
  );
}

createRoot(ROOT_ELEMENT).render(<App />);
