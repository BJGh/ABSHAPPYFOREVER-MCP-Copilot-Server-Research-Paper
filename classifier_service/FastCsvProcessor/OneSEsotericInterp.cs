using System;
using System.Collections.Generic;
using System.Linq;

public class OneSInterpreter
{
    private byte[] dataSegment;
    private int dataPointer;
    private int acc;
    private Marketplace market;
    public void RunBackboneMain(string srcDir = "backbonebrains/src")
    {
        try
        {
            var files = System.IO.Directory.GetFiles(srcDir, "*.java");
            if (files.Length == 0)
            {
                Console.WriteLine($"No .java files found in '{srcDir}'.");
                return;
            }

            // Compile all Java sources
            var compileArgs = "-d \"" + srcDir + "\" " + string.Join(" ", files.Select(f => $"\"{f}\""));
            var compileInfo = new System.Diagnostics.ProcessStartInfo("javac", compileArgs)
            {
                RedirectStandardOutput = true,
                RedirectStandardError = true,
                UseShellExecute = false
            };

            using (var p = System.Diagnostics.Process.Start(compileInfo))
            {
                var outp = p.StandardOutput.ReadToEnd();
                var err = p.StandardError.ReadToEnd();
                p.WaitForExit();
                if (!string.IsNullOrEmpty(outp)) Console.WriteLine(outp);
                if (!string.IsNullOrEmpty(err)) Console.WriteLine(err);
                if (p.ExitCode != 0)
                {
                    Console.WriteLine("javac failed with exit code " + p.ExitCode);
                    return;
                }
            }

            // Run the compiled Main class
            var runInfo = new System.Diagnostics.ProcessStartInfo("java", "-cp \"" + srcDir + "\" Main")
            {
                RedirectStandardOutput = true,
                RedirectStandardError = true,
                UseShellExecute = false
            };

            using (var p = System.Diagnostics.Process.Start(runInfo))
            {
                var outp = p.StandardOutput.ReadToEnd();
                var err = p.StandardError.ReadToEnd();
                p.WaitForExit();
                if (!string.IsNullOrEmpty(outp)) Console.WriteLine(outp);
                if (!string.IsNullOrEmpty(err)) Console.WriteLine(err);
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine("RunBackboneMain failed: " + ex.Message);
        }
    }
    public OneSInterpreter(int dataLen = 1024)
    {
        dataSegment = new byte[dataLen];
        dataPointer = 0;
        acc = 0;
        market = new Marketplace();
        market.RegisterTrader("You", 1000);
        market.RegisterTrader("TseTseg", 500);
    }

    // Script uses line-separated commands:
    // UP/DOWN/LEFT/RIGHT, SET <value>, OFFER <seller> <item> <price>, LIST, BUY <buyer> <offerId>, BALANCE <name>, INVENTORY <name>
    public void ExecuteScript(int dataLen, string script)
    {
        if (dataLen != dataSegment.Length)
            dataSegment = new byte[dataLen];

        var lines = script
            .Split(new[] { '\r', '\n' }, StringSplitOptions.RemoveEmptyEntries)
            .Select(l => l.Trim())
            .Where(l => l.Length > 0);

        foreach (var line in lines)
        {
            var parts = line.Split(' ', StringSplitOptions.RemoveEmptyEntries);
            var cmd = parts[0].ToUpperInvariant();

            switch (cmd)
            {
                case "UP": acc++; break;
                case "DOWN": acc--; break;
                case "RIGHT": dataPointer = (dataPointer + 1) % dataSegment.Length; break;
                case "LEFT": dataPointer = (dataPointer - 1 + dataSegment.Length) % dataSegment.Length; break;
                case "SET":
                    if (parts.Length >= 2 && int.TryParse(parts[1], out int v)) dataSegment[dataPointer] = (byte)v;
                    break;
                case "OFFER":
                    // OFFER <seller> <item> <price>
                    if (parts.Length >= 4 && int.TryParse(parts[3], out int price))
                    {
                        var seller = parts[1];
                        var item = parts[2];
                        var id = market.CreateOffer(seller, item, price);
                        Console.WriteLine($"Offer #{id}: {seller} offers {item} @ {price}");
                    }
                    else Console.WriteLine("Bad OFFER syntax. Use: OFFER <seller> <item> <price>");
                    break;
                case "LIST":
                    foreach (var o in market.ListOffers()) Console.WriteLine(o);
                    break;
                case "BUY":
                    // BUY <buyer> <offerId>
                    if (parts.Length >= 3 && int.TryParse(parts[2], out int oid))
                    {
                        var buyer = parts[1];
                        var ok = market.Buy(buyer, oid);
                        Console.WriteLine(ok ? $"BUY OK: {buyer} bought offer #{oid}" : $"BUY FAILED: {buyer} could not buy #{oid}");
                    }
                    else Console.WriteLine("Bad BUY syntax. Use: BUY <buyer> <offerId>");
                    break;
                case "BALANCE":
                    if (parts.Length >= 2) Console.WriteLine($"{parts[1]} balance: {market.GetBalance(parts[1])}");
                    break;
                case "INVENTORY":
                    if (parts.Length >= 2) Console.WriteLine($"{parts[1]} inventory: {string.Join(',', market.GetInventory(parts[1]))}");
                    break;
                default:
                    Console.WriteLine($"Unknown command: {line}");
                    break;
            }

            // Let TseTseg act a bit after each command
            market.SimulateTseTsegBehavior();
        }

        Console.WriteLine($"Execution done. Pointer={dataPointer}, Acc={acc}");
    }

    // Minimal marketplace for trading between traders (including TseTseg)
    private class Marketplace
    {
        private static readonly Random rnd = new Random();
        private int nextId = 1;
        private Dictionary<int, Offer> offers = new Dictionary<int, Offer>();
        private Dictionary<string, Trader> traders = new Dictionary<string, Trader>(StringComparer.OrdinalIgnoreCase);

        public int CreateOffer(string seller, string item, int price)
        {
            if (!traders.ContainsKey(seller)) RegisterTrader(seller, 0);
            var id = nextId++;
            offers[id] = new Offer { Id = id, Seller = seller, Item = item, Price = price };
            return id;
        }

        public IEnumerable<string> ListOffers()
        {
            foreach (var o in offers.Values) yield return $"#{o.Id} {o.Item} by {o.Seller} @ {o.Price}";
        }

        public bool Buy(string buyer, int id)
        {
            if (!offers.TryGetValue(id, out var o)) return false;
            if (!traders.ContainsKey(buyer)) RegisterTrader(buyer, 0);
            var seller = traders[o.Seller];
            var b = traders[buyer];
            if (b.Balance < o.Price) return false;
            b.Balance -= o.Price;
            seller.Balance += o.Price;
            b.Inventory.Add(o.Item);
            offers.Remove(id);
            return true;
        }

        public void RegisterTrader(string name, int balance) => traders[name] = new Trader { Name = name, Balance = balance };

        public int GetBalance(string name) => traders.TryGetValue(name, out var t) ? t.Balance : 0;

        public IEnumerable<string> GetInventory(string name) => traders.TryGetValue(name, out var t) ? t.Inventory : Enumerable.Empty<string>();

        public void SimulateTseTsegBehavior()
        {
            const string tse = "TseTseg";
            if (!traders.ContainsKey(tse)) RegisterTrader(tse, 500);

            // If TseTseg has no active offers, create one occasionally
            if (!offers.Values.Any(o => o.Seller.Equals(tse, StringComparison.OrdinalIgnoreCase)) && rnd.NextDouble() < 0.5)
            {
                CreateOffer(tse, $"tr_item{nextId}", rnd.Next(20, 200));
            }

            // Occasionally try buying a cheap non-self offer
            var cheap = offers.Values.Where(o => !o.Seller.Equals(tse, StringComparison.OrdinalIgnoreCase)).OrderBy(o => o.Price).FirstOrDefault();
            if (cheap != null && traders[tse].Balance >= cheap.Price && rnd.NextDouble() < 0.2)
            {
                Buy(tse, cheap.Id);
                Console.WriteLine($"TseTseg auto-bought #{cheap.Id} ({cheap.Item})");
            }
        }

        private class Offer { public int Id; public string Seller; public string Item; public int Price; }
        private class Trader { public string Name; public int Balance; public List<string> Inventory = new List<string>(); }
    }
}

public static class Program
{
    public static void Main()
    {
        var interpreter = new OneSInterpreter(1024);
        string script = @"
OFFER You potion 100
OFFER TseTseg scroll 80
LIST
BUY You 2
BALANCE You
BALANCE TseTseg
INVENTORY You
INVENTORY TseTseg
";
        interpreter.ExecuteScript(1024, script);
    }
}