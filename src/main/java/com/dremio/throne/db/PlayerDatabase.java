/*
 * Copyright (C) 2017-2019 Dremio Corporation. This file is confidential and private property.
 */

package com.dremio.throne.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDatabase {

  private final Map<String, Player> players = new ConcurrentHashMap<>();
  private final Map<String, String> classes = new ConcurrentHashMap<>(PlayerClassLoader.loadPlayerClasses());

  public void addPlayer(Player player) {
    players.put(player.getName(), player);
  }

  public Player getPlayer(String name) {
    return players.get(name);
  }

  public List<Player> getAllPlayers() {
    return new ArrayList<>(players.values());
  }

}
