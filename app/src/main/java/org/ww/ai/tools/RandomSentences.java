package org.ww.ai.tools;

import java.util.Random;

public enum RandomSentences {
    RANDOM_SENTENCES;

    private static final String RAW_SENTENCES = "Gampfus trollige Bätschel seimeln plongwöpfig vopffiks krauer Zwöftras.\n" +
            "Trüpfiwipp trubbelduff zwerdertürp dreipfinsch eiklopsem knarschelwumms.\n" +
            "Zibberplumpf kraftwöppig krepeldumpf klonzwicker flubbelnöck.\n" +
            "Wurzelschlabber wippelzwicker fratzelgrüppel schwingeldapp wolkentunk.\n" +
            "Tralalupf flingelduff schrubberknuff quiekeldampf zirpeldudel.\n" +
            "Bombeleum blubberklumpf kitzelknack klapspudel rumpelklirr.\n" +
            "Zickelzopf schrabbelplumps wippelbrumm rinkelbums trolliwupf.\n" +
            "Kitzeldibbel klirreklirr zwirbelgrinz hoppeldippel fumpelknarz.\n" +
            "Brimbelbratz klopferdutz klapperplumpf schnattergrummel gorgelzwumpf.\n" +
            "Schwappeltrumpf spitzelwumm fimmeltrippel klingelkraxel brubbeltrumpf.\n" +
            "Schnippelklapp pluckerzwack rasselzipp trullerzwumm glippschlonz.\n" +
            "Zipfelgrimpf krimmelkrampf fratzelbratz klockenspumpf blubberschnibb.\n" +
            "Piffelplaff klapserblubb spitzeldipf knibbelschnack fratzeldumpf.\n" +
            "Knirschrumpf schwappeldibbel krackselklipp schnibbertripp wuppelplumms.\n" +
            "Zimmeleum flappeldipp schlapperknick trömmelkack schrubberknack.\n" +
            "Gimmeldapp zwickerflump knatterblubb zickeldibbel schlawellbrumm.\n" +
            "Wuffelplapp gimpelduff schrabbelklirr krummelkrampf rumpelfrumpf.\n" +
            "Dillerdall knuffelknuff sappeltruller schwippelschwapp wubbelklapp.\n" +
            "Zockelwapp schwingelklirr brabbeldimpf schnatterflump klingelsplumpf.\n" +
            "Fratzelschnurp schlappelknuff knopfelschlabber quakelbrumm wippelwumpf.\n" +
            "Zirpelsplumpf krabbeldibbel pampelbratz trömmelknack kitzeldupf.\n" +
            "Gampfus tropflig blibbeldapp schrabbelzwack plingelschnibb.\n" +
            "Wuppelwamm blubberknuff klirrerschnack klapselfumpf drömmelklipp.\n" +
            "Pimpeldumpf zippelgrumpf fratzeldussel zappelklipp klingeldopf.\n" +
            "Knurrblubb zwippelschwapp flippeldupp klappergrumpf trolliplumms.\n" +
            "Rumpelzopp kribbelzwack schwippeldipp knallerplumpf klapperknuff.\n" +
            "Zickelsplumpf plumperknick knisterblubb schnatterwumm wumpelknall.\n" +
            "Zirbelklimper piffeldapp gummeltrippel schrabbelplimpfl zippelflumpf.";
    private final Random mRandom = new Random();

    public String getRandomSentence() {
        String[] all = RAW_SENTENCES.split("\\n");
        int which = mRandom.nextInt(all.length - 1);
        return all[which];
    }

}
