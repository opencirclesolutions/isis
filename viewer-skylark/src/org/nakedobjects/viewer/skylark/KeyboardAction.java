package org.nakedobjects.viewer.skylark;

public class KeyboardAction {
    public static final int NONE = 0;
    public static final int ABORT = 1;
    public static final int NEXT_VIEW = 2;
    public static final int NEXT_WINDOW= 3;
    public static final int PREVIOUS_VIEW = 4;
    public final static int PREVIOUS_WINDOW= 5;
    
    final int keyCode;
    final int modifiers;
    private boolean isConsumed;
    
    public KeyboardAction(int keyCode, int modifiers) {
        this.keyCode = keyCode;
        this.modifiers = modifiers;
        isConsumed = false;
    }
    
    public int getKeyCode() {
        return keyCode;
    }
    public int getModifiers() {
        return modifiers;
    }

    public boolean isConsumed() {
        return isConsumed;
    }
    
    public void consume() {
        isConsumed = true;
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */