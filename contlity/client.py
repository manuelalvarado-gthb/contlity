"""
client.py — ContlityClient, clase base del SDK de Contlity.

Uso rápido:
    from contlity import ContlityClient

    # Desde variable de entorno CONTLITY_API_KEY (recomendado)
    client = ContlityClient.from_env()

    # O explícito
    client = ContlityClient("ctly_live_...")

    print(client.account.info())
    print(client.credits.balance())
    jobs = client.sat.jobs()
"""
from __future__ import annotations

import os

import requests

from .exceptions import AuthError, NotFoundError, RateLimitError, CreditError, PlanError, ContlityError
from .resources.account   import AccountResource
from .resources.clients   import ClientsResource
from .resources.cfdis     import CFDIsResource
from .resources.sat       import SATResource
from .resources.sql       import SQLResource
from .resources.workflows import WorkflowsResource
from .resources.webhooks  import WebhooksResource
from .resources.credits   import CreditsResource
from .resources.cargas    import CargasResource

__version__ = "0.3.0"

ENV_VAR = "CONTLITY_API_KEY"


class ContlityClient:
    """
    Cliente oficial del SDK de Contlity.

    Inicialización
    --------------
    Recomendado — desde variable de entorno:

        export CONTLITY_API_KEY="ctly_live_abc123..."

        from contlity import ContlityClient
        client = ContlityClient.from_env()

    Explícito (no recomendado en código fuente):

        client = ContlityClient("ctly_live_abc123...")

    Parámetros
    ----------
    api_key:  API key con formato ctly_live_... — créala en /desarrollador.
    base_url: URL base (default: https://contlity-api.tech-mx.cloud/api).
              Úsala para self-hosted o staging.
    timeout:  Timeout HTTP en segundos (default: 30).

    Recursos disponibles
    --------------------
    client.account   → info de cuenta
    client.clients   → clientes / RFCs
    client.cfdis     → catálogo de CFDIs
    client.sat       → SAT Descarga Masiva
    client.sql       → SQL fiscal
    client.workflows → workflows
    client.webhooks  → suscripciones webhook
    client.credits   → balance de créditos
    """

    DEFAULT_BASE_URL = "https://contlity-api.tech-mx.cloud/api"

    def __init__(
        self,
        api_key:  str,
        base_url: str | None = None,
        timeout:  int = 30,
    ):
        if not api_key or not api_key.startswith("ctly_"):
            raise ValueError("api_key inválida. Formato esperado: ctly_live_...")
        self._api_key  = api_key
        self._base_url = (base_url or self.DEFAULT_BASE_URL).rstrip("/")
        self._timeout  = timeout

        self._session = requests.Session()
        self._session.headers.update({
            "Authorization": f"Bearer {api_key}",
            "User-Agent":    f"contlity-python/{__version__}",
            "Accept":        "application/json",
            "Content-Type":  "application/json",
        })

        # Recursos
        self.account   = AccountResource(self)
        self.clients   = ClientsResource(self)
        self.cfdis     = CFDIsResource(self)
        self.sat       = SATResource(self)
        self.sql       = SQLResource(self)
        self.workflows = WorkflowsResource(self)
        self.webhooks  = WebhooksResource(self)
        self.credits   = CreditsResource(self)
        self.cargas    = CargasResource(self)

    # ── Constructores alternativos ────────────────────────────────────────────

    @classmethod
    def from_env(
        cls,
        env_var:  str       = ENV_VAR,
        base_url: str | None = None,
        timeout:  int        = 30,
    ) -> "ContlityClient":
        """
        Inicializa el cliente leyendo la API key de una variable de entorno.

        La variable por defecto es ``CONTLITY_API_KEY``.  Es la forma recomendada
        de autenticarse: nunca hardcodees keys en el código fuente.

        Uso::

            # ~/.bashrc o .env
            export CONTLITY_API_KEY="ctly_live_abc123..."

            from contlity import ContlityClient
            client = ContlityClient.from_env()
            print(client.account.info())

        Args:
            env_var:  Nombre de la variable de entorno (default: CONTLITY_API_KEY).
            base_url: URL base opcional (default: https://contlity-api.tech-mx.cloud/api).
            timeout:  Timeout HTTP en segundos (default: 30).

        Raises:
            EnvironmentError: si la variable de entorno no está definida o está vacía.
        """
        key = os.environ.get(env_var, "").strip()
        if not key:
            raise EnvironmentError(
                f"La variable de entorno '{env_var}' no está definida o está vacía.\n"
                f"Defínela antes de usar ContlityClient.from_env():\n\n"
                f"  export {env_var}=\"ctly_live_...\"\n\n"
                f"O pásala explícitamente: ContlityClient(api_key=\"ctly_live_...\")"
            )
        return cls(api_key=key, base_url=base_url, timeout=timeout)

    # ── Utilidades ────────────────────────────────────────────────────────────

    def ping(self) -> dict:
        """
        Verifica la conexión y validez de la API key haciendo una petición
        ligera a GET /v1/account.

        Returns:
            dict con account_id, name y plan si la key es válida.

        Raises:
            AuthError: si la key es inválida o revocada.
            ContlityError: si hay un problema de red.

        Uso::

            from contlity import ContlityClient
            client = ContlityClient.from_env()
            info = client.ping()
            print(f"Conectado como: {info['name']} (plan: {info['plan']})")
        """
        return self.account.info()

    def __repr__(self) -> str:
        masked = self._api_key[:12] + "..." if len(self._api_key) > 12 else "***"
        return f"ContlityClient(api_key=\"{masked}\", base_url=\"{self._base_url}\")"

    # ── Método base ───────────────────────────────────────────────────────────

    def _request(self, method: str, path: str, **kwargs) -> dict:
        """
        Hace una petición a /v1{path} y retorna el JSON parseado.
        Traduce códigos HTTP a excepciones del SDK.
        """
        url = f"{self._base_url}/v1{path}"
        try:
            resp = self._session.request(
                method, url, timeout=self._timeout, **kwargs
            )
        except requests.exceptions.Timeout:
            raise ContlityError(f"Timeout después de {self._timeout}s en {method} {path}")
        except requests.exceptions.ConnectionError as exc:
            raise ContlityError(f"Error de conexión: {exc}")

        self._raise_for_status(resp)
        try:
            return resp.json()
        except Exception:
            return {"raw": resp.text}

    def _raise_for_status(self, resp: requests.Response) -> None:
        if resp.ok:
            return
        try:
            body = resp.json()
        except Exception:
            body = {"message": resp.text}

        detail  = body.get("detail") or body
        message = (
            detail.get("message") if isinstance(detail, dict) else str(detail)
        ) or resp.reason

        sc = resp.status_code
        if sc == 401:
            raise AuthError(message, status_code=sc, response=body)
        if sc == 402:
            d = detail if isinstance(detail, dict) else {}
            raise CreditError(message, balance=d.get("balance", 0),
                               cost=d.get("cost", 0), plan=d.get("plan", ""))
        if sc == 403:
            raise PlanError(message, status_code=sc, response=body)
        if sc == 404:
            raise NotFoundError(message, status_code=sc, response=body)
        if sc == 429:
            d = detail if isinstance(detail, dict) else {}
            raise RateLimitError(message, retry_after=d.get("retry_after", 60))
        raise ContlityError(message, status_code=sc, response=body)
