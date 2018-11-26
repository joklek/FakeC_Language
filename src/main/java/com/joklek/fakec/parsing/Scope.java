package com.joklek.fakec.parsing;

import com.joklek.fakec.parsing.error.ScopeError;
import com.joklek.fakec.parsing.types.DataType;
import com.joklek.fakec.tokens.Token;

import java.util.HashMap;
import java.util.Map;

public class Scope {

    // TODO discern between variable and function
    private final Scope parentScope;
    private final Map<Token, DataType> members;

    /**
     * Creates a scope with a provided parent scope
     * @param parentScope parent scope
     */
    public Scope(Scope parentScope) {
        this.parentScope = parentScope;
        this.members = new HashMap<>();
    }

    /**
     * Creates a root scope with parent scope null
     */
    public Scope() {
        this.parentScope = null;
        this.members = new HashMap<>();
    }

    /**
     * Adds a name to scope
     * @param name name of variable, function or etc.
     * @param node the node of provided name
     */
    public ScopeError add(Token name, DataType node) {
        if(containsName(name)) {
            return new ScopeError("Duplicate name found in scope", name);
        }
        else {
            members.put(name, node);
        }
        return null;
    }

    /**
     * Resolve scope for name. Returns node for given name if it exists. Else throws {@link ScopeError}
     * @param name name to be resolved
     * @return node of resolved name
     */
    public DataType resolve(Token name) {
        if(containsName(name)) {
            return members.get(name);
        }
        else if (parentScope != null) {
            return parentScope.resolve(name);
        }
        else {
            throw new ScopeError("Variable or function not found in current scope", name);
        }
    }

    // TODO think of better way of doing this
    private boolean containsName(Token name) {
        for(Token key: members.keySet()) {
            if(key.getLexeme().equals(name.getLexeme())) {
                return true;
            }
        }
        return false;
    }
}
