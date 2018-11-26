package com.joklek.fakec.parsing;

import com.joklek.fakec.parsing.error.ScopeError;
import com.joklek.fakec.parsing.types.Node;

import java.util.*;

public class Scope {

    private final Scope parentScope;
    private final Map<String, Node> members;

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
    public ScopeError add(String name, Node node) {
        if(members.containsKey(name)) {
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
    public Node resolve(String name) {
        if(members.containsKey(name)) {
            return members.get(name);
        }
        else if (parentScope != null) {
            return parentScope.resolve(name);
        }
        else {
            throw new ScopeError("Variable or function not found in current scope", name);
        }
    }
}
