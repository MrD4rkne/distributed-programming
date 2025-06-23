#include <iostream>
#include <random>
#include <map>

using namespace std;

void generate_multi_set(const int n, const int d)
{
    map<int, int> counts;
    std::mt19937 rng(std::random_device{}());
    std::uniform_int_distribution<int> dist(1, d);

    for (int i = 0; i < n; i++)
    {
        counts[dist(rng)]++;
    }

    for (const auto &[key, value] : counts)
    {
        for (int i = 0; i < value; i++)
        {
            cout << key << " ";
        }
    }
    cout << endl;
}

int main()
{
    ios::sync_with_stdio(false);
    cin.tie(NULL);

    const int MAX_T_POWER = 2;
    const int MAX_D = 20;                  // 50 max
    const int MAX_ELEMS_PER_MULTISET = 10; // 100 max

    std::mt19937 rng(std::random_device{}());
    std::uniform_int_distribution<int> dist_t(0, MAX_T_POWER);
    std::uniform_int_distribution<int> dist_d(20, MAX_D);
    std::uniform_int_distribution<int> dist_n(0, MAX_ELEMS_PER_MULTISET);

    int t = dist_t(rng);
    int d = dist_d(rng);
    int n = dist_n(rng);
    int m = dist_n(rng);

    cout << (int)pow(2, t) << " " << d << " " << n << " " << m << endl;
    generate_multi_set(n, d);
    generate_multi_set(m, d);

    return 0;
}
