package controller;

import java.util.ArrayList;

import model.Lobby;

public class Controller {

    private static Controller instance = new Controller();

    public static Controller getInstance() {
        if (instance == null) {
            instance = new Controller();
        }

        return instance;
    }

    public Controller(){}

    public ArrayList<Lobby> getLobbys() {

        ArrayList<Lobby> lobbyList = new ArrayList<Lobby>();
        ArrayList<Integer> testAL = new ArrayList<Integer>();
        testAL.add(1);
        testAL.add(3);
        testAL.add(4);

        Lobby testLobby = new Lobby(1, "testlobby", testAL);
        Lobby testLobby2 = new Lobby(2, "testlobby", testAL);
        Lobby testLobby3 = new Lobby(3, "testlobby", testAL);

        lobbyList.add(testLobby);
        lobbyList.add(testLobby2);
        lobbyList.add(testLobby3);

        return lobbyList;
    }

}
