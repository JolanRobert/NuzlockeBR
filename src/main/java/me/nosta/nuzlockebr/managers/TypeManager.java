package me.nosta.nuzlockebr.managers;

import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.nztypes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TypeManager {

    private static TypeManager Instance;
    public static TypeManager getInstance() {
        if (Instance == null) Instance = new TypeManager();
        return Instance;
    }

    public NZType noneType = new None();
    public List<NZType> typeList = new ArrayList<>();

    public TypeManager() {
        typeList.add(new Acier());
        typeList.add(new Combat());
        typeList.add(new Dragon());
        typeList.add(new Eau());
        typeList.add(new Electrique());
        typeList.add(new Fee());
        typeList.add(new Feu());
        typeList.add(new Glace());
        typeList.add(new Insecte());
        typeList.add(new Normal());
        typeList.add(new Plante());
        typeList.add(new Poison());
        typeList.add(new Psy());
        typeList.add(new Spectre());
        typeList.add(new Tenebres());
        typeList.add(new Vol());
    }

    public NZType getRandomType() {
        Random rdm = new Random();
        int index = rdm.nextInt(typeList.size());
        NZType nzType = typeList.get(index);
        while (nzType.getNZPlayer() != null) {
            index = (index+1)%typeList.size();
            nzType = typeList.get(index);
        }
        return nzType;
    }
}
