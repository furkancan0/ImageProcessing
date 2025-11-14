CREATE OR REPLACE FUNCTION images_tsvector_trigger() RETURNS trigger AS $$
BEGIN
  NEW.search_vector :=
    to_tsvector('english', COALESCE(NEW.name,'') || ' ' || COALESCE(NEW.description,''));
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS images_vector_update ON images;

CREATE TRIGGER images_vector_update
    BEFORE INSERT OR UPDATE ON images
                         FOR EACH ROW
                         EXECUTE FUNCTION images_tsvector_trigger();

-- And create GIN index for FTS
CREATE INDEX IF NOT EXISTS images_search_idx ON images USING GIN (search_vector);