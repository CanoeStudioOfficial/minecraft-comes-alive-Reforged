package mca.entity;

import mca.enums.EnumAgeState;

public interface VillagerDimensions {
    float getWidth();

    float getHeight();

    float getBreasts();

    float getHead();

    final class Mutable implements VillagerDimensions {
        private float width;
        private float height;
        private float breasts;
        private float head;

        public Mutable(VillagerDimensions dimensions) {
            set(dimensions);
        }

        @Override
        public float getWidth() {
            return width;
        }

        @Override
        public float getHeight() {
            return height;
        }

        @Override
        public float getBreasts() {
            return breasts;
        }

        @Override
        public float getHead() {
            return head;
        }

        public void interpolate(VillagerDimensions a, VillagerDimensions b, float f) {
            width = a.getWidth() + (b.getWidth() - a.getWidth()) * f;
            height = a.getHeight() + (b.getHeight() - a.getHeight()) * f;
            breasts = a.getBreasts() + (b.getBreasts() - a.getBreasts()) * f;
            head = a.getHead() + (b.getHead() - a.getHead()) * f;
        }

        public void set(VillagerDimensions a) {
            width = a.getWidth();
            height = a.getHeight();
            breasts = a.getBreasts();
            head = a.getHead();
        }
    }
}
