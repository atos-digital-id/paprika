
OUT=../resources/images/asy

ASY=$(wildcard *.asy)
SVG=$(patsubst %.asy,$(OUT)/%.svg,$(ASY))

all: $(SVG)

$(SVG): $(OUT)/%.svg: %.asy
	asy -f svg $^
	mkdir -p $(OUT)
	mv $(basename $^).svg $(OUT)

clean:
	rm -f $(SVG)

mrproper: clean

