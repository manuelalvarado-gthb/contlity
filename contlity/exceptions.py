"""Excepciones del SDK de Contlity."""
from __future__ import annotations


class ContlityError(Exception):
    """Error base del SDK de Contlity."""
    def __init__(self, message: str, status_code: int | None = None, response: dict | None = None):
        super().__init__(message)
        self.status_code = status_code
        self.response    = response or {}

    def __repr__(self) -> str:
        return f"{self.__class__.__name__}({self.args[0]!r}, status_code={self.status_code})"


class AuthError(ContlityError):
    """API key inválida, revocada o sin permisos."""


class NotFoundError(ContlityError):
    """Recurso no encontrado (HTTP 404)."""


class RateLimitError(ContlityError):
    """Límite de peticiones alcanzado (HTTP 429). Reintentar después de retry_after segundos."""
    def __init__(self, message: str, retry_after: int = 60):
        super().__init__(message, status_code=429)
        self.retry_after = retry_after


class CreditError(ContlityError):
    """Créditos insuficientes para la operación (HTTP 402)."""
    def __init__(self, message: str, balance: int = 0, cost: int = 0, plan: str = ""):
        super().__init__(message, status_code=402)
        self.balance = balance
        self.cost    = cost
        self.plan    = plan


class PlanError(ContlityError):
    """La operación requiere un plan superior (HTTP 403)."""


class WebhookError(ContlityError):
    """Error al verificar la firma de un webhook entrante."""
