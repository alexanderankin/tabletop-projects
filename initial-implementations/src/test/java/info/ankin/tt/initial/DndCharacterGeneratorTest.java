package info.ankin.tt.initial;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static info.ankin.tt.initial.DndCharacterGenerator.Ability.*;

class DndCharacterGeneratorTest {

    DndCharacterGenerator generator = new DndCharacterGenerator();

    @Test
    void generateSeventh() {
        Map<DndCharacterGenerator.Ability, Integer> abScores = generator.genAbilityScores(new TreeSet<>(List.of(INT, WIS, CHA, DEX, CON, STR)));
        System.out.println(abScores);
    }
}
