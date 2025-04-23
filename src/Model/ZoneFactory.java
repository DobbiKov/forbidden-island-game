package Model;

import Errors.AllTheCardsAreUsedException;

import java.util.HashSet;
import java.util.Random;

public class ZoneFactory {

    private HashSet<ZoneCard> used_cards ;

    public ZoneFactory() {
        used_cards = new HashSet<>();
    }


    protected ZoneCard getAvaliableCard() {
        if(used_cards.size() >= 24){
            throw new AllTheCardsAreUsedException();
        }
        Random rand = new Random();
        int x = rand.nextInt(24);
        ZoneCard zc = ZoneCard.fromInt(x);
        while(used_cards.contains(zc)) {
            x = rand.nextInt(24);
            zc = ZoneCard.fromInt(x);
        }
        return zc;
    }

    public Zone createRandomZone(int x, int y){
        ZoneCard zc = getAvaliableCard();
        used_cards.add(zc);
        Zone zone = null;
        switch (zc){
            case fodls_landing:
                zone = this.createHelicopterZone(x, y);
                break;

            case bronze_gate:
            case copper_gate:
            case gold_gate:
            case iron_gate:
            case silver_gate:
                zone = this.createPlayerZone(x, y, zc);
                break;


            // earth stone
            case temle_of_the_sun:
            case temple_of_the_moon:
                zone = this.createArtefactZone(x, y, zc, Artefact.Earth);
                break;

                // wind huyaynd artefact
            case howling_garden:
            case whispering_garden:
                zone = this.createArtefactZone(x, y, zc, Artefact.Wind);
                break;

                // fire huyayer artefact
            case cave_of_shadows:
            case cave_of_embers:
                zone = this.createArtefactZone(x, y, zc, Artefact.Fire);
                break;

                // water huyoter artefact
            case tidal_palace:
            case coral_palace:
                zone = this.createArtefactZone(x, y, zc, Artefact.Water);
                break;

            default:
                zone = this.createSimpleZone(x, y, zc);
                break;
        }
        return zone;
    }
    public Zone createInaccessibleZone(int x, int y){
        return new Zone(x, y, false, null);
    }

    public Zone createPlayerZone(int x, int y, ZoneCard card){
        return new PlayerStartZone(x, y, card);
    }
    public Zone createHelicopterZone(int x, int y){
        return new HelicopterZone(x, y);
    }
    public Zone createArtefactZone(int x, int y, ZoneCard card, Artefact artefact){
        return new ArtefactZone(x, y, card, artefact);

    }
    public Zone createSimpleZone(int x, int y, ZoneCard card){
        return new Zone(x, y, true, card);
    }
}
