package com.joklek.fakec.scope;

import com.joklek.fakec.parsing.ast.NodeWithType;
import com.joklek.fakec.parsing.types.element.ElementType;
import com.joklek.fakec.scope.error.ScopeError;
import com.joklek.fakec.tokens.Token;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that checks holds scope information
 */
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
    public ScopeError add(Token name, NodeWithType node, ElementType elementType) {
        try {
            // If name can't be resolved, then we can add it
            resolve(name, elementType);
        }
        catch (ScopeError e) {
            members.put(name, new DataHolder(elementType, node));
            return null;
        }
        String elementTypeString = elementType.toString().toLowerCase();
        return new ScopeError(String.format("Duplicate %s name found in scope", elementTypeString), name);
    }

    /**
     * Resolve scope for name. Returns node for given name if it exists. Else throws {@link ScopeError}
     * @param name name to be resolved
     * @param elementType clarifies if it's variable, function, or etc.
     * @return node of resolved name
     */
    public NodeWithType resolve(Token name, ElementType elementType) {
        DataHolder dataHolder = getToken(name, elementType);

        if(dataHolder != null) {
            NodeWithType node = dataHolder.getNode();
            ElementType realType = dataHolder.getElementType();
            if(realType.equals(elementType)) {
                return node;
            }
        }

        if (parentScope != null) {
            return parentScope.resolve(name, elementType);
        }
        else {
            String elementTypeString = elementType.toString().substring(0,1).toUpperCase() + elementType.toString().substring(1).toLowerCase();
            throw new ScopeError(String.format("%s not found in current scope", elementTypeString), name);
        }
    }

    private DataHolder getToken(Token name, ElementType type) {
        DataHolder data = null;
        for (Map.Entry<Token, DataHolder> entry : members.entrySet()) {
            if(entry.getKey().getLexeme().equals(name.getLexeme())
                    && entry.getValue().elementType.equals(type)) {
                data = entry.getValue();
                break;
            }
        }
        return data;
    }

    class DataHolder {
        private final ElementType elementType;
        private final NodeWithType node;

        DataHolder(ElementType elementType, NodeWithType node) {
            this.elementType = elementType;
            this.node = node;
        }

        public ElementType getElementType() {
            return elementType;
        }

        public NodeWithType getNode() {
            return node;
        }
    }
}
