package fabuco.performer;

public enum PerformerCallState {
  IN_PROGRESS,
  SUCCEED,
  EXPIRED,
  EXPIRED_WITHOUT_TRY,
  IRRECOVERABLE_ERROR,
  RECOVERABLE_ERROR;
}
