package model;

public class CompositionLink extends LinkObject {

    public CompositionLink(BasicObject from, int fromPort, BasicObject to, int toPort) {
        super(from, fromPort, to, toPort, new DiamondStyle());
    }
}
