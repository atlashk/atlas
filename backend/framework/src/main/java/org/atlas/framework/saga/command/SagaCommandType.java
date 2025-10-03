package org.atlas.framework.saga.command;

public enum SagaCommandType {

  CREATE_ORDER,
  RESERVE_PRODUCT,
  INITIALIZE_PAYMENT,
  PROCESS_PAYMENT,
  SEND_NOTIFICATION
}
