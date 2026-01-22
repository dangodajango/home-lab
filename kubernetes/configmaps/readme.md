# ConfigMap

## Theory

Our services often have different configurations depending on the environment in which they run. For example, we might
run them locally and connect to a local database rather than one used in test or production. Accordingly, each
environment has a different set of environment variables.

One way to achieve this is to build different Docker images (one per environment) and specify the configurations there.
However, this approach introduces unnecessary overhead, as we then need to manage multiple images and configuration
files.

A better practice is to keep our images free of any environment-specific properties and provide those properties at
runtime. In a Kubernetes context, one way to do this is by using a ConfigMap.

The ConfigMap object is very simple: it allows us to define configuration as key-value pairs and inject them into Pods
either as environment variables or as files.

## Declarative approach

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: game-demo
data:
  player_initial_lives: "3"
  ui_properties_file_name: "user-interface.properties"

  game.properties: |
    enemy.types=aliens,monsters
    player.maximum-lives=5    
  user-interface.properties: |
    color.good=purple
    color.bad=yellow
    allow.textmode=true    
```

Here we have two regular key-value pairs, player_initial_lives and ui_properties_file_name, as well as game.properties
and user-interface.properties, which are slightly different. These are intended to be injected as files via a volume
that can be mounted by the Pod.

For these file-based entries, we configure the file name directly as the key and the file contents as the value. When
the value spans multiple lines, we can use the | character, which tells YAML that the following indented lines are part
of the same value.

To use the ConfigMap, we need a Pod specification. This can be defined either directly in a Pod or within a template (
for example, in a Deployment):

### Injecting specific environment variables

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: configmap-demo-pod
spec:
  containers:
    - name: demo
      image: alpine
      command: [ "sleep", "3600" ]
      env:
        - name: PLAYER_INITIAL_LIVES
          valueFrom:
            configMapKeyRef:
              name: game-demo
              key: player_initial_lives
        - name: UI_PROPERTIES_FILE_NAME
          valueFrom:
            configMapKeyRef:
              name: game-demo
              key: ui_properties_file_name
```

Note that we use the env field here. There is also an envFrom field, which is used to inject all entries of a ConfigMap
as environment variables. However, with env, we can selectively choose individual key-value pairs from the ConfigMap.

### Injecting all key-value pairs from a ConfigMap as environment variables

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: env-configmap
spec:
  containers:
    - name: app
      command: [ "/bin/sh", "-c", "printenv" ]
      image: busybox:latest
      envFrom:
        - configMapRef:
            name: configmap-demo-pod
```

With this approach, the game.properties and user-interface.properties entries will be created as environment variables
named GAME_PROPERTIES and USER_INTERFACE_PROPERTIES. Kubernetes replaces unsupported special characters (such as
dashes - and dots .) with underscores and converts the names to uppercase.

The contents of these variables will be the multiline values from the ConfigMap entries, preserved as a single
concatenated string. For example:

```shell
GAME_PROPERTIES="enemy.types=aliens,monsters
player.maximum-lives=5"
```

### Mounting the ConfigMap pairs as files

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: configmap-demo-pod
spec:
  containers:
    - name: demo
      image: alpine
      command: [ "sleep", "3600" ]
      volumeMounts:
        - name: config
          mountPath: "/config"
          readOnly: true
  volumes:
    - name: config
      configMap:
        name: game-demo
        items:
          - key: "game.properties"
          - key: "user-interface.properties"   
```

This will create two files in the mounted directory - game.properties and user-interface.properties, and write the
contents
of their corresponding values into them. If we omit the items array, Kubernetes will create a file for each key-value
pair in the ConfigMap, even if the value is a single line.

## Imperative approach

There are a few ways to create a ConfigMap imperatively. We can either define each key-value pair individually, or we
can use an existing configuration file and create the ConfigMap as if the entire file were the value of a single key (
for example, game.properties).

```shell
kubectl create configmap my-config \
  --from-file=app.properties \
  --from-literal=LOG_LEVEL=INFO \
  --dry-run=client -o yaml
```

## Source

https://kubernetes.io/docs/concepts/configuration/configmap/