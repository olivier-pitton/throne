/*
 * Copyright (C) 2017-2019 Dremio Corporation. This file is confidential and private property.
 */

package com.dremio.throne.db;

import java.util.Objects;

public class Player implements Comparable<Player> {

  private String dateStr;
  private String name;
  private String className;
  private String guild;
  private long kills;
  private long assists;
  private long damageDone;
  private long damageReceived;
  private long healing;
  private boolean valid;

  public Player(String name, String guild, String dateStr, Long... numeric) {
    this.name = name;
    this.guild = guild;
    this.dateStr = dateStr;
    this.valid = numeric.length == 5;
    if (numeric.length > 1) {
      this.kills = numeric[0];
    }
    if (numeric.length > 2) {
      this.assists = numeric[1];
    }
    if (numeric.length > 3) {
      this.damageDone = numeric[2];
    }
    if (numeric.length > 4) {
      this.damageReceived = numeric[3];
    }
    if (numeric.length >= 5) {
      this.healing = numeric[4];
    }
  }

  public boolean isValid() {
    return valid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getGuild() {
    return guild;
  }

  public void setGuild(String guild) {
    this.guild = guild;
  }

  public long getKills() {
    return kills;
  }

  public void setKills(long kills) {
    this.kills = kills;
  }

  public long getAssists() {
    return assists;
  }

  public void setAssists(long assists) {
    this.assists = assists;
  }

  public long getDamageDone() {
    return damageDone;
  }

  public void setDamageDone(long damageDone) {
    this.damageDone = damageDone;
  }

  public long getDamageReceived() {
    return damageReceived;
  }

  public void setDamageReceived(long damageReceived) {
    this.damageReceived = damageReceived;
  }

  public long getHealing() {
    return healing;
  }

  public void setHealing(long healing) {
    this.healing = healing;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Player))
      return false;
    Player player = (Player) o;
    return Objects.equals(name, player.name);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name);
  }

  @Override
  public String toString() {
    return name;
  }

  public String toCSV() {
    StringBuilder csv = new StringBuilder();
    csv.append(dateStr).append(",");
    csv.append(guild).append(",");
    csv.append(name).append(",");
    csv.append(className).append(",");
    csv.append(kills).append(",");
    csv.append(assists).append(",");
    csv.append(damageDone).append(",");
    csv.append(damageReceived).append(",");
    csv.append(healing);
    return csv.toString();
  }

  @Override
  public int compareTo(Player o) {
    return Long.compare(o.kills, kills);
  }
}
