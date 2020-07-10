{ pkgs, stdenv, lib, fetchFromGitHub
# Dependencies
, xcodeWrapper
, writeScript
, androidPkgs
, newScope
, git 
, platform ? "android"
, arch ? "386"
, api ? "23" } :


let
  callPackage = newScope {};
  src = pkgs.fetchgit {
    url = "https://github.com/status-im/nim-status";
    rev = "918a3d4389d58ea8b5de85fc9fb5c8c704c63659";
    sha256 = "0p3s65dkxphcpkz6hnacfd7l5aj9b80mcgcmlqjfz4121dggyhai";
    fetchSubmodules = false;
  };

  flags = callPackage ./getFlags.nix {platform = platform; arch = arch;};
  nimBase = ./nimbase.h;
in 
  stdenv.mkDerivation rec {
  name = "nim-status-go_lib";
  inherit src;
  buildInputs = with pkgs; [ nim ];

  phases = ["unpackPhase" "preBuildPhase" "buildPhase" "installPhase"];

  preBuildPhase = ''
    echo 'switch("passC", "${flags.compiler}")' >> config.nims
    echo 'switch("passL", "${flags.linker}")' >> config.nims
    echo 'switch("cpu", "${flags.nimCpu}")' >> config.nims
    echo 'switch("os", "${flags.nimPlatform}")' >> config.nims

    mkdir ./nim_status/c/go/include
    cp ${nimBase} ./nim_status/c/go/include/nimbase.h
  '';

  buildPhase = ''
    ${flags.vars}
    echo -e "Building Nim-Status"
    clang --version
    export INCLUDE_PATH=./include
    # Need -d:nimEmulateOverflowChecks,
    # otherwise compiler will complain about
    # undefined nimMulInt/nimAddInt functions
    # https://github.com/nim-lang/Nim/issues/13645#issuecomment-601037942
    # https://github.com/nim-lang/Nim/pull/13692
    nim c \
      --cincludes:$INCLUDE_PATH \
      --app:staticLib \
      --header \
      --nimcache:nimcache/nim_status_go \
      --noMain \
      -d:nimEmulateOverflowChecks \
      --threads:on \
      --tlsEmulation:off \
      -o:nim_status_go.a \
      nim_status/c/go/shim.nim
  '';

  installPhase = ''
    mkdir $out
    cp nimcache/nim_status_go/shim.h $out/nim_status.h
    cp ./nim_status/c/go/include/nimbase.h $out/
    mv nim_status_go.a $out/libnim_status.a
  '';
}
