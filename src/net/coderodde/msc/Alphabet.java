package net.coderodde.msc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Alphabet<C> implements Iterable<C> {

    private final Set<C> alphabet;
    private final List<C> characterList;
    
    public Alphabet(C... chars) {
        Objects.requireNonNull(chars, "The array of characters is null.");  
        this.alphabet = new LinkedHashSet<>();
        
        for (C ch : chars) {
            this.alphabet.add(ch);
        }
        
        this.characterList = new ArrayList<>(this.alphabet);
    }
    
    public C get(int index) {
        return this.characterList.get(index);
    }
    
    public int size() {
        return this.alphabet.size();
    }
    
    public boolean containsCharacter(C ch) {
        return this.alphabet.contains(ch);
    }
    
    public int getNumberOfNonemptyCharacterCombinations() {
        return 1 << this.alphabet.size() - 1;
    }

    @Override
    public Iterator<C> iterator() {
        return Collections.<C>unmodifiableSet(this.alphabet).iterator();
    }
}
