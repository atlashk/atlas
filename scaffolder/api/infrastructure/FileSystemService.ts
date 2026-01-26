
import { FileNode } from '../models/types';

export class FileSystemService {
  private root: FileNode;

  constructor(rootName: string) {
    this.root = {
      name: rootName,
      type: 'directory',
      path: rootName,
      children: [],
    };
  }

  public addFile(path: string, content: string): void {
    const parts = path.split('/').filter(p => p.length > 0);
    let currentNode = this.root;
    let currentPath = this.root.name;

    // Navigate to the directory, creating if doesn't exist
    for (let i = 0; i < parts.length - 1; i++) {
      const part = parts[i];
      currentPath += '/' + part;
      
      let child = currentNode.children?.find(c => c.name === part);
      if (!child) {
        child = {
          name: part,
          type: 'directory',
          path: currentPath,
          children: [],
        };
        if (!currentNode.children) {
            currentNode.children = [];
        }
        currentNode.children.push(child);
      }
      currentNode = child;
    }

    // Add the file
    const fileName = parts[parts.length - 1];
    const fileNode: FileNode = {
      name: fileName,
      type: 'file',
      path: currentPath + '/' + fileName,
      content: content,
      size: content.length,
    };
    
    if (!currentNode.children) {
        currentNode.children = [];
    }
    // Overwrite if exists
    const existingIndex = currentNode.children.findIndex(c => c.name === fileName);
    if (existingIndex >= 0) {
        currentNode.children[existingIndex] = fileNode;
    } else {
        currentNode.children.push(fileNode);
    }
  }

  public getTree(): FileNode {
    return this.root;
  }

  public getFileContent(path: string): string | null {
      // Logic to find file content by path
      // path should be like "projectName/src/main/..."
      // But user might request "src/main/..." if root is implicit? 
      // Let's assume path includes root name or we handle it.
      
      // Recursive search
      const findNode = (node: FileNode, searchPath: string): FileNode | null => {
          if (node.path === searchPath) return node;
          if (node.children) {
              for (const child of node.children) {
                  const result = findNode(child, searchPath);
                  if (result) return result;
              }
          }
          return null;
      }
      
      const node = findNode(this.root, path);
      return node?.content || null;
  }
}
