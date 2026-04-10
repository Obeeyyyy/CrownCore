---
description: Basic information about YAML.
---

# YAML 101

## Array <a href="#array" id="array"></a>

A `YAML Array` can be represented in mutliple ways:

```yaml
# this is an empty array
my-empty-array: []

# filled it might look like this
my-filled-array-1: [a, b, c] 

# or this
my-filled-array-2: 
  - a
  - b
  - c
```

In Minecraft plugin configurations, the block style (-) is strongly recommended for readability.

## Map (Object/Section) <a href="#map-objectsection" id="map-objectsection"></a>

Most plugin configs are structured as maps (also called sections):

```yaml
mysql:
  host: localhost
  port: 3306
  database: minecraft
```

This is equivalent to:

```yaml
mysql: { host: localhost, port: 3306, database: minecraft }
```

The block style should always be preferred.

## Indentation <a href="#indentation" id="indentation"></a>

YAML uses indentation to represent structure. This is VERY IMPORTANT.

Correct:

```yaml
features:
  chat:
    enabled: true
```

Wrong:

```yaml
features:
 chat:
    enabled: true
```

Roles you should follow:

* Only use spaces no tabs
* Keep your style consistent

Incorrect indentation is one of the most common reasons why Minecraft plugins fail to load their configuration properly. The Server will also complain with an error message in the console when a configuration file could not be loaded.

## Comments <a href="#comments" id="comments"></a>

```yaml
# This is a comment
```

Comments are ignored by the parser. This means they have no effect at all. For minecraft plugin configuration, the comments often get lost. So if all the comments in your file suddenly vanish. Dont worry, this is normal.

## Strings <a href="#strings" id="strings"></a>

```yaml
name: Obey
prefix: "[Founder]"
```

Quotes are optional in most cases. You should use quotes when:

* the value contains a colon `:`
* the value starts with a special char
* the value contains leading or tracing spaces
* the value contains color codes

Correct:

```yaml
shop: "Shop: Main Menu"

discord: "&e&o Checkout our Discord Server."
```

Wrong:

```yaml
shop: Shop: Main Menu

discord: &e&o Checkout our Discord Server.
```

## Numbers <a href="#numbers" id="numbers"></a>

```yaml
amount: 5
price: 12.5

# Numbers are parsed as numeric values.
# Do not quote numbers unless the plugin explicitly expects a string.
```

## Booleans <a href="#booleans" id="booleans"></a>

```yaml
enabled: true
debug: false

# yaml also allows the following: 
#  yes / no
#  on / off

# i recommend you stick to the classic true/false
```
