package com.plavonra.common.audit;

public enum Action {
  CREATED("CREATED"),
  UPDATED("UPDATED"),
  DELETED("DELETED");

  private final String name;

  private Action(String value) {
    this.name = value;
  }

  public String value() {
    return this.name;
  }

  @Override
  public String toString() {
    return name;
  }
}
