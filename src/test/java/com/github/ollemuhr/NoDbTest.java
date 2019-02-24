package com.github.ollemuhr;

public class NoDbTest extends UserTest {

  private static final Config CONFIG = new TestConf().config();

  @Override
  Config getConfig() {
    return CONFIG;
  }
}
