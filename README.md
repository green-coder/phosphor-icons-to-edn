# Phosphor Icons to EDN

A ClojureScript/Node project for exporting the Phosphor Icons to EDN for Clojure(script) consumption.

The project is extracting the icons' SVG and their metadata directly from the npm package `@phosphor-icons/core`.

Update the version in `package.json` to export the latest icons.

## Run the export

In this project's folder:

```bash
npm install
npm run build
node out/main.js <output-directory>
```

## Similar projects

- [Phosphor icons as Hiccup](https://github.com/cjohansen/phosphor-clj) by Christian Johansen.

## License

This project is distributed under the Eclipse Public License v2.0.

Copyright (c) Vincent Cantin and contributors.
