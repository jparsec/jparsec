java -jar closure-compiler/compiler.jar \
    --warning_level VERBOSE --language_in ECMASCRIPT5 \
    --compilation_level ADVANCED_OPTIMIZATIONS \
    --js src/prettify.js \
    --externs closure-compiler/console-externs.js \
    --externs closure-compiler/amd-externs.js \
    --externs js-modules/externs.js \
    `find src -name "lang-*.js" -printf " --js %p"` \
    | perl -pe 's/\bPR\.PR_ATTRIB_NAME\b/"atn"/g; \
                s/\bPR\.PR_ATTRIB_VALUE\b/"atv"/g; \
                s/\bPR\.PR_COMMENT\b/"com"/g; \
                s/\bPR\.PR_DECLARATION\b/"dec"/g; \
                s/\bPR\.PR_KEYWORD\b/"kwd"/g; \
                s/\bPR\.PR_LITERAL\b/"lit"/g; \
                s/\bPR\.PR_PLAIN\b/"pln"/g; \
                s/\bPR\.PR_PUNCTUATION\b/"pun"/g; \
                s/\bPR\.PR_STRING\b/"str"/g; \
                s/\bPR\.PR_TAG\b/"tag"/g; \
                s/\bPR\.PR_TYPE\b/"typ"/g;' > p.js
