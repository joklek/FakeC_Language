package com.joklek.fakec.parsing;

import com.joklek.fakec.parsing.error.ScopeError;
import com.joklek.fakec.parsing.types.data.DataType;
import com.joklek.fakec.parsing.types.element.ElementType;
import com.joklek.fakec.tokens.Token;

import java.util.HashMap;
import java.util.Map;

public class Scope {

    private final Scope parentScope;
    private final Map<Token, DataHolder> members;

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
     * @param elementType clarifies if it's variable, function, or etc.
     */
    public ScopeError add(Token name, DataType node, ElementType elementType) {
        try {
            // If name can't be resolved, then we can add it
            resolve(name, elementType);
        }
        catch (ScopeError e) {
            members.put(name, new DataHolder(elementType, node));
            return null;
        }
        return new ScopeError("Duplicate name found in scope", name);
    }

    /**
     * Resolve scope for name. Returns node for given name if it exists. Else throws {@link ScopeError}
     * @param name name to be resolved
     * @param elementType clarifies if it's variable, function, or etc.
     * @return node of resolved name
     */
    public DataType resolve(Token name, ElementType elementType) {
        DataType type = containsName(name, elementType);
        if(type != null) {
            return type;
        }
        else if (parentScope != null) {
            return parentScope.resolve(name, elementType);
        }
        else {
            throw new ScopeError("Variable or function not found in current scope", name);
        }
    }

    // TODO think of better way of doing this
    private DataType containsName(Token name, ElementType elementType) {
        for(Map.Entry<Token, DataHolder> entry: members.entrySet()) {
            if(entry.getKey().getLexeme().equals(name.getLexeme())
                    && entry.getValue().getElementType().equals(elementType)) {
                return entry.getValue().getType();
            }
        }
        return null;
    }

    class DataHolder {
        private final ElementType elementType;
        private final DataType type;

        DataHolder(ElementType elementType, DataType type) {
            this.elementType = elementType;
            this.type = type;
        }

        public ElementType getElementType() {
            return elementType;
        }

        public DataType getType() {
            return type;
        }
    }
}
