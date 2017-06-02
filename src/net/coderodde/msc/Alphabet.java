package net.coderodde.msc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class Alphabet<C> implements Iterable<C> {

    private final Set<C> alphabet;
    
    public Alphabet(C... chars) {
        Objects.requireNonNull(chars, "The array of characters is null.");  
        this.alphabet = new LinkedHashSet<>();
        
        for (C ch : chars) {
            this.alphabet.add(ch);
        }
    }
    
    public boolean containsCharacter(C ch) {
        return this.alphabet.contains(ch);
    }
    
    public int getNumberOfNonemptyCharacterCombinations() {
        return 1 << alphabet.size() - 1;
    }

    @Override
    public Iterator<C> iterator() {
        return Collections.<C>unmodifiableSet(this.alphabet).iterator();
    }
}
