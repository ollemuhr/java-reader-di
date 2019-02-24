package com.github.ollemuhr;

public class WithDbTest extends UserTest {

  private static final Config CONFIG = new TestWithDbConf().config();

  @Override
  Config getConfig() {
    return CONFIG;
  }
}
